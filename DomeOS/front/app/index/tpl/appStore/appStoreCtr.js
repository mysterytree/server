domeApp.controller('appStoreCtr', ['$scope', '$domeAppStore', function($scope, $domeAppStore) {
	$scope.$emit('pageTitle', {
		title: '欢迎来到应用商店！',
		descrition: '在这里您可以选择需要的应用并快速部署。部署后请到部署模块查询您的应用。',
		mod: 'appStore'
	});
	$domeAppStore.getStoreApps().then(function(res) {
		$scope.appList = res.data || [];
	});
}]);