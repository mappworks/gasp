angular.module('jasp.util', ['ngLodash'])
.filter('titleOrName', function() {
  return function(obj) {
      return obj.title || obj.name;
  };
})
.directive('focus', function() {
  return function(scope, el, attrs) {
    attrs.$observe('focus', function(newVal) {
      if (newVal === true || newVal === 'true') {
        el[0].focus();
      }
    });
  };
})
.factory('_', function(lodash) {
  return lodash;
})
.run(function() {
  // some utility functions
  String.prototype.startsWith = function(s) {
    return this.indexOf(s) == 0;
  };

  String.prototype.endsWith = function(s) {
    return this.substr(-s.length) == s;
  };
});