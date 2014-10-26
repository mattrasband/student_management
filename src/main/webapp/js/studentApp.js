angular.module('studentApp', ['ui.bootstrap'])
    // Create and handle the confirm dialog (uses JavaScript's
    // built in rather than something pretty)
    .factory("confirm", function($window, $q) {
        // Define promise-based confirm() method.
        function confirm(message) {
            var defer = $q.defer();
            if ($window.confirm(message)) {
                defer.resolve(true);
            } else {
                defer.reject(false);
            }

            return defer.promise;
        }
        return confirm;
    })

    // Create the student api, consumer for api routes
    .factory('studentApi', function($http, $window) {
        // Ensure that all urls are relative to wherever we are deployed (i.e. tomcat containers
        // prepending paths)
        var prePath = $window.location.pathname.replace(/\/$/, '');
        return {
            create: function(first, last) {
                return $http.post(prePath + '/api/students', {
                        first: first,
                        last: last
                    });
            },

            getAll: function() {
                return $http.get(prePath + '/api/students');
            },

            get: function(studentId) {
                return $http.get(prePath + '/api/students/' + studentId);
            },

            del: function(studentId) {
                return $http.delete(prePath + '/api/students/' + studentId);
            },

            update: function(studentId, student) {
                return $http.put(prePath + '/api/students/' + studentId, student);
            }
        };
    })

    // Control a user input box for editing students
    .controller('ModalController', function($scope, $modalInstance, student) {
        // Copy to avoid editing object in main module's student array.
        $scope.student = {};
        angular.copy(student, $scope.student);

        $scope.ok = function () {
            $modalInstance.close($scope.student);
        };

        $scope.cancel = function () {
            $modalInstance.dismiss();
        };
    })

    // CRUD for students
    .controller('StudentController', function($scope, confirm, studentApi, $modal) {
        $scope.students = [];

        $scope.showEditMenu = function($index) {
            var wasActive = $scope.students[$index].isActive;
            for (var i = 0; i < $scope.students.length; i++) {
                if (i != $index) {
                    $scope.students[i].isActive = false;
                } else {
                    // undefined/null/false are all false-y, no need for an explicit check.
                    $scope.students[$index].isActive = !wasActive;
                }
            }
        };

        // Create a new student.
        $scope.createUser = function() {
            if ($scope.newStudent.$valid) {
                studentApi.create($scope.newFirstName, $scope.newLastName)
                    .success(function(student) {
                        console.debug('Created new student: ' + JSON.stringify(student));
                        $scope.newFirstName = '';
                        $scope.newLastName = '';
                        $scope.showAddMenu = false;
                        $scope.students.push(student);
                    })
                    .error(function(data, status, headers, config) {
                        console.error('Error creating new student.');
                    });
            } else {
                console.debug('Invalid user creation request, unable to process.');
            }
        };

        // Delete a student.
        $scope.delUser = function(student) {
            confirm("Are you sure you want to delete '" + student.last + ", " + student.first + "'?")
                .then(
                    function(res) {
                        studentApi.del(student.id)
                            .success(function() {
                                for (var i = 0; i < $scope.students.length; i++) {
                                    if (student.id == $scope.students[i].id) {
                                        $scope.students.splice(i, 1);
                                        break;
                                    }
                                }
                            })
                            .error(function(data, status, headers, config) {
                                console.error('Error deleting the student:' + data);
                            });
                    },
                    function(res) {
                        // This is highly unlikely to happen, but I've seen apps crash due to silly things
                        // like this.
                        try {
                            console.debug('Cancelled user deletion: ' + JSON.stringify(student));
                        } catch (e) {
                            console.debug('Issue stringifying json: ' + e);
                        }
                    }
                );
        };

        // Update a student
        $scope.updateUser = function(student, $index) {
            var modalInstance = $modal.open({
                templateUrl: 'partials/userEditModal.html',
                controller: 'ModalController',
                size: 'sm',
                resolve: {
                    student: function() {
                        return student;
                    }
                }
            });

            modalInstance.result.then(
                function(updatedStudent) {
                    console.debug('Updating Student: ' + JSON.stringify(updatedStudent));
                    studentApi.update(updatedStudent.id, updatedStudent)
                        .success(function(result) {
                        console.debug('Update Success');
                            $scope.students[$index] = result;
                        })
                        .error(function(data, status, headers, config) {
                            console.error('Error during update!');
                        })
                },
                function() {
                    console.log('cancelled');
                }
            );
        };

        // Refresh the student list
        $scope.refresh = function() {
            $scope.students = [];
            $scope.loaderTimeout = false;

            // Get all the users, populates the student list on load.
            studentApi.getAll()
                .success(function(students) {
                    for (var i = 0; i < students.length; i++) {
                        $scope.students.push(students[i]);
                    }
                    $scope.loaderTimeout = true;
                })
                .error(function(data, status, headers, config) {
                    alert('Unable to get students, please try again later.');
                    $scope.loaderTimeout = true;
                });
        };

        $scope.closeNewUserInput = function() {
            $scope.newLastName = '';
            $scope.newFirstName = '';
            $scope.showAddMenu = !$scope.showAddMenu;
        };

        $scope.refresh();
    });