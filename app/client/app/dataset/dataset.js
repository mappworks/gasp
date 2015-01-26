angular.module('gasp.dataset', [
  'gasp.dataset.new',
  'gasp.dataset.edit',
  'gasp.dataset.settings',
  'gasp.dataset.schema'
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
