angular.module('studentApp', ['ui.bootstrap'])

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

    // Control a pretty stylized modal
    .controller('ModalController', function($scope, $modalInstance, student, title) {
        // Copy to avoid editing object in main module's student array.
        $scope.student;
        $scope.title = title;
        if (student) {
            $scope.student = {};
            angular.copy(student, $scope.student);
        }

        $scope.ok = function () {
            $modalInstance.close($scope.student);
        };

        $scope.cancel = function () {
            $modalInstance.dismiss();
        };
    })

    // CRUD for students
    .controller('StudentController', function($scope, studentApi, $modal) {
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
            var modalInstance = $modal.open({
                template: [
                    '<div>',
                        '<div class="modal-header"><h3 class="modal-title">{{title}}</h3></div>',
                        '<div class="modal-footer">',
                            '<button class="btn btn-primary" ng-click="ok()">OK</button>',
                            '<button class="btn btn-warning" ng-click="cancel()">Cancel</button>',
                        '</div>',
                    '</div>'].join(''),
                controller: function($scope, $modalInstance, title) {
                    $scope.title = title;
                    $scope.ok = function() {
                        $modalInstance.close();
                    };
                    $scope.cancel = function() {
                        $modalInstance.dismiss();
                    };
                },
                size: 'sm',
                resolve: {
                    title: function() {
                        return "Are you sure?";
                    }
                }
            });

            modalInstance.result.then(
                function() {
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
                function() {
                    console.debug("Cancelled student delete: " + student.id);
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
                    },
                    title: function() {
                        return "Edit Student";
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
