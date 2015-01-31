describe('DatasetEditCtrl', function() {
  var scope, createCtrl, http, basePath;

  beforeEach(module('gasp.dataset.edit'));

  beforeEach(inject(function(
    $controller, $rootScope, $timeout, $log, $q, $httpBackend, _, App, Api) {

    basePath = App.BasePath;
    scope = $rootScope.$new();
    http = $httpBackend;

    createCtrl = function(stateParams) {
      var map = $q.defer();
      var ctrl = $controller('DatasetEditCtrl', {
        $scope: scope,
        $state: {},
        $stateParams: stateParams,
        $timeout: $timeout,
        $modal: {},
        $localStorage: {},
        $log: $log,
        _: _,
        leafletData: {
          getMap: function() {
            return map.promise;
          }
        },
        Api: Api
      });
      map.resolve({
        setView: function(){},
        on: function(){},
        getBounds: function() {
          return {
            toBBoxString: function() {
              return '-180,-90,180.-90';
            }
          };
        },
        getSize: function() {
          return {x: 512, y: 512};
        }
      });
      return ctrl;
    };

  }));

  beforeEach(function() {
    http.when('GET', /api\/query/).respond(200);
    http.when('GET', basePath + '/api/datasets/1').respond(200, {
      name: 'foo',
      query: 'select * from foo',
      params: []
    });
  });

  it('Loads properly', function() {
    var ctrl = createCtrl({
      id: '1'
    });
    http.flush();
    expect(scope.dataset).toBeDefined();
  });

  it('Adds parameters from query', function() {
    var ctrl = createCtrl({
      id: '1'
    });
    http.flush();

    expect(scope.params).toBeUndefined();

    scope.updateParamsFromQuery('SELECT * FROM foo WHERE name = ${name}');
    expect(scope.params.length).toBe(1);

    // var param = scope.dataset.params[0];
    // expect(param.name).toBe('name');
    // expect(param.type).toBe('String');
  });

});
