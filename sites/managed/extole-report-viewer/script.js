// Module definition
const app = angular.module('jsonFormatterApp', ['jsonFormatter']);

// Controller definition
app.controller('JsonController', ['$scope', function($scope) {
    getData().then(data => {
        $scope.jsonData = data; 
        $scope.$apply(); // Apply scope changes as getData() is async
    });
}]);