
/* global $*/
angular.module('gasp.api', [])
.factory('Api', function($http, $q) {
  var apiRoot = '/api';

  function http(config) {
     var d = $q.defer();
     $http(config)
        .success(function(data, status, headers, config) {
          d.resolve({
            success: status >= 200 && status < 300,
            status: status,
            data: data
          });
        })
        .error(function(data, status, headers, config) {
          d.reject({status: status, data: data});
        });
      return d.promise;
  }

  return {
    dataset: {
      list: function(opts) {
        return http({
          method: 'GET',
          url: apiRoot + '/datasets?' + $.param(opts||{})
        });
      },
      get: function(id) {
        return http({
          method: 'GET',
          url: apiRoot + '/datasets/' + id
        });
      },
      put: function(dataset) {
        return http({
          method: 'PUT',
          url: apiRoot + '/datasets/' + dataset.id,
          data: dataset
        });
      }
    }
  };
});
