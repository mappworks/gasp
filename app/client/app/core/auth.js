/* global $ */
angular.module('jasp.auth', ['jasp.event'])
.factory('Auth', function($http, $q, $state, $rootScope, AppEvent) {
  var Auth = {};

  Auth.session = function() {
    var d = $q.defer();
    $http.get('/auth/session')
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
        $rootScope.$broadcast(AppEvent.Login, session);
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
      url: '/auth/login',
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
      url: '/auth/logout',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    }).success(function(data, status, headers, config) {
      $rootScope.$broadcast(AppEvent.Logout);
      $state.go('login');
    });
  };

  return Auth;
});