angular.module('gasp.constant',[])
.constant('App', {
  BasePath: '/gasp',
  Event: {
      Login: 'app-login',
      Logout: 'app-logout'
  }
});
