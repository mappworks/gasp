/* global $ */
angular.module('gasp.auth', ['gasp.constant'])
.factory('Auth', function($http, $q, $state, $rootScope, App) {
  var Auth = {};

  Auth.session = function() {
    var d = $q.defer();
    $http.get(App.BasePath + '/auth/session')
      .success(function(data, status, headers, config) {
        d.resolve(data);
      })
      .error(function() {
        d.reject();
      });
    return d.promise;
  };

  Auth.sessionOrLogin = function() {
    var d = $q.defer();
    Auth.session().then(
      function(session) {
        $rootScope.$broadcast(App.Event.Login, session);
        d.resolve(session);
      },
      function() {
        $state.go('login');
      });
    return d.promise;
  };

  Auth.login = function(username, password) {
    var d = $q.defer();
    $http({
      method: 'POST',
      url: App.BasePath + '/auth/login',
      data: $.param({
        'username': username,
        'password': password
      }),
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    }).success(function(data, status, headers, config) {
      d.resolve();
    }).error(function(data, status, headers, config) {
      d.reject();
    });
    return d.promise;
  };

  Auth.logout = function() {
    $http({
      method: 'POST',
      url: App.BasePath + '/auth/logout',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    }).success(function(data, status, headers, config) {
      $rootScope.$broadcast(App.Event.Logout);
      $state.go('login');
    });
  };

  return Auth;
});
