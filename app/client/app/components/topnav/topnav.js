angular.module('gasp.topnav', ['ui.bootstrap', 'gasp.constant', 'gasp.auth'])
  .directive('topnav', function() {
    return {
      restrict: 'E',
      templateUrl: 'components/topnav/topnav.tpl.html',
      controller: function($scope, $log, App, Auth, Api) {
        $scope.$on(App.Event.Login, function(evt, session) {
          $scope.session = session;
          Api.info.get().then(function(result) {
            $scope.appinfo = result.data;
          });
        });
        $scope.$on(App.Event.Logout, function(evt) {
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