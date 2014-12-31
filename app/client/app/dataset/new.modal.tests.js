describe('DatasetNewCtrl', function() {
  var scope, createCtrl, http, modalInstance, basePath;

  beforeEach(module('gasp.dataset.new'));

  beforeEach(inject(function($controller, $rootScope, $httpBackend, App, Api) {

    basePath = App.BasePath;
    scope = $rootScope.$new();
    http = $httpBackend;
    modalInstance = {
      close: function() {},
      dismiss: function() {}
    };

    createCtrl = function() {
      return $controller('DatasetNewCtrl', {
        $scope: scope,
        $modalInstance: modalInstance,
        Api: Api
      });
    };

  }));

  it('Saves on ok' , function() {
    http.expect('POST', basePath + '/api/datasets/')
      .respond(201, '{"id":"1"}');

    spyOn(modalInstance, 'close');
    var ctrl = createCtrl();

    scope.ok();
    http.flush();

    expect(modalInstance.close).toHaveBeenCalledWith('1');
  });

  it('Dismisses on cancel' , function() {
    spyOn(modalInstance, 'dismiss');
    createCtrl();

    scope.cancel();
    expect(modalInstance.dismiss).toHaveBeenCalledWith('cancel');
  });
});