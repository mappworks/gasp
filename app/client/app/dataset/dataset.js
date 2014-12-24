angular.module('jasp.dataset', [
  'jasp.dataset.edit'
])
.config(function($stateProvider) {
  $stateProvider
    .state('dataset', {
      abstract: true,
      resolve: {
        session: function(Auth) {
          return Auth.sessionOrLogin();
        }
      },
      url: '/dataset',
      template: '<ui-view/>'
    })
    .state('dataset.edit', {
      url: '/:id/edit',
      templateUrl: 'dataset/edit.tpl.html',
      controller: 'DatasetEditCtrl'
    });
});