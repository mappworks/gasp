angular.module('gasp.home', [
  'gasp.topnav', 'gasp.core', 'gasp.login', 'gasp.dataset'
])
.config(
  function($stateProvider) {
    $stateProvider
      .state('home', {
        resolve: {
          session: function(Auth) {
            return Auth.sessionOrLogin();
          }
        },
        url: '/',
        templateUrl: 'home/home.tpl.html',
        controller: 'HomeCtrl'
      });
  })
.controller('HomeCtrl',
  function($scope, $state, $log, $modal, Api, session) {
    $scope.session = session;

    $scope.newDataset = function() {
      $modal.open({
        templateUrl: 'dataset/new.modal.tpl.html',
        controller: 'DatasetNewCtrl',
        backdrop: 'static'
      }).result.then(function(newId) {
        $state.go('dataset.edit', {id: newId});
      });
    };

    Api.dataset.list({
      sort: 'modified-', limit: 3
    }).then(function(result) {
      $scope.datasets = result.data;
    });
  });
