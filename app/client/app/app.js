angular.module('gasp', [
  'ui.bootstrap',
  'ui.router',
  'ui.codemirror',
  'ngLodash',
  'gettext',
  'gasp.core',
  'gasp.login',
  'gasp.home',
  'gasp.dataset'
])
.config(
  function($urlRouterProvider) {
    $urlRouterProvider.otherwise('/');
  })
.controller('AppCtrl',
  function($scope) {
    $scope.state = {};
    $scope.$on('$stateChangeSuccess',
      function(e, to, toParams, from, fromParams) {
        $scope.state.curr = {name: to, params: toParams};
        $scope.state.prev = {name: from, params: fromParams};
      });
  })
.run(function(editableOptions, editableThemes) {
  editableOptions.theme = 'default';
  editableThemes['default'].submitTpl =
    '<i class="fa fa-check-circle clickable" ng-click="$form.$submit()"></i>';
  editableThemes['default'].cancelTpl =
    '<i class="fa fa-times-circle clickable" ng-click="$form.$cancel()"></i>';
});


