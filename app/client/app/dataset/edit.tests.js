describe('DatasetEditCtrl', function() {
  var scope, createCtrl, http, basePath;

  beforeEach(module('gasp.dataset.edit'));

  beforeEach(inject(function(
    $controller, $rootScope, $timeout, $log, $q, $httpBackend, _, App, Api) {

    basePath = App.BasePath;
    scope = $rootScope.$new();
    http = $httpBackend;

    createCtrl = function(stateParams) {
      return $controller('DatasetEditCtrl', {
        $scope: scope,
        $state: {},
        $stateParams: stateParams,
        $timeout: $timeout,
        $modal: {},
        $log: $log,
        _: _,
        leafletData: {
          getMap: function() {
            return $q.defer().promise;
          }
        },
        Api: Api
      });
    };

  }));

  beforeEach(function() {
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
