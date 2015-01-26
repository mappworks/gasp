angular.module('gasp.util', ['ngLodash'])
.filter('titleOrName', function() {
  return function(obj) {
      return obj.title || obj.name;
  };
})
.directive('focus', function($timeout, $parse) {
  // http://stackoverflow.com/questions/14833326/how-to-set-focus-on-input-field
  return {
    link: function(scope, element, attrs) {
      var model = $parse(attrs.focus);
      scope.$watch(model, function(value) {
        if(value === true) {
          $timeout(function() {
            element[0].focus();
          });
        }
      });
      // element.bind('blur', function() {
      //    scope.$apply(model.assign(scope, false));
      // });
    }
  };
  // return function(scope, el, attrs) {
  //   attrs.$observe('focus', function(newVal) {
  //     if (newVal === true || newVal === 'true') {
  //       el[0].focus();
  //     }
  //   });
  // };
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
