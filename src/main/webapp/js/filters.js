'use strict';

/* Filters */

angular.module('myApp.filters', []).
  filter('interpolate', ['version', function(version) {
    return function(text) {
      return String(text).replace(/\%VERSION\%/mg, version);
    }
  }]);

/* 自定义的过滤器（用来将带html标签的内容 正确绑定到ng-model上）
 * author：tai
 */
//angular.module('myApp.filters', []).
//filter('trustHtml', function($sce) {
//    return function (input){
//        return $sce.trustAsHtml(input); 
//    } 
//});


//myApp.filter("trustHtml",function($sce){
//	   return function (input){
//	       return $sce.trustAsHtml(input); 
//	   } 
//	});


angular.module('myApp.filters', []).
filter("trustHtml",function($sce){
	//alert(input)
	   return function (input){
	       return $sce.trustAsHtml(input); 
	   } 
	});
  