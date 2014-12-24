angular.module('gasp.topnav', ['ui.bootstrap', 'gasp.event', 'gasp.auth'])
  .directive('topnav', function() {
    return {
      restrict: 'E',
      templateUrl: '/components/topnav/topnav.tpl.html',
      controller: function($scope, $log, AppEvent, Auth) {
        $scope.$on(AppEvent.Login, function(evt, session) {
          $scope.session = session;
        });
        $scope.$on(AppEvent.Logout, function(evt) {
          $scope.session = null;
        });
        $scope.logout = function() {
          Auth.logout();
        };
        $scope.keypress = function(evt) {
          if (evt.which === 13) {
            $log.log('Enter!');
          }
        };
      }
    };
  });
