package org.domeos.api.controller.global;

import org.domeos.api.controller.ApiController;
import org.domeos.api.model.global.WebSsh;
import org.domeos.api.service.global.WebConsoleService;
import org.domeos.basemodel.HttpResponseTemp;
import org.domeos.shiro.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by baokangwang on 2016/1/16.
 */
@Controller
@RequestMapping("/")
public class WebConsoleController extends ApiController {

    @Autowired
    WebConsoleService webConsoleService;

    @ResponseBody
    @RequestMapping(value = "console", method = RequestMethod.GET)
    public void getWebConsole(@RequestParam(value="host", required=true) String host,
                              @RequestParam(value="container", required=false) String container,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        webConsoleService.getWebConsole(host, container, request, response);
    }

    @ResponseBody
    @RequestMapping(value = "console", method = RequestMethod.POST)
    public void postWebConsole(HttpServletRequest request,
                               HttpServletResponse response) {
        webConsoleService.postWebConsole(request, response);
    }

    @ResponseBody
    @RequestMapping(value = "api/global/webssh", method = RequestMethod.GET)
    public HttpResponseTemp<?> getWebsshSetting() {
        long userId = AuthUtil.getUserId();
        return webConsoleService.getWebsshSetting(userId);
    }

    @ResponseBody
    @RequestMapping(value = "api/global/webssh", method = RequestMethod.POST)
    public HttpResponseTemp<?> setWebsshSetting(@RequestBody WebSsh webSsh) {
        long userId = AuthUtil.getUserId();
        return webConsoleService.setWebsshSetting(webSsh, userId);
    }

    @ResponseBody
    @RequestMapping(value = "api/global/webssh", method = RequestMethod.PUT)
    public HttpResponseTemp<?> modifyWebsshSetting(@RequestBody WebSsh webSsh) {
        long userId = AuthUtil.getUserId();
        return webConsoleService.updateWebsshSetting(webSsh, userId);
    }

    @ResponseBody
    @RequestMapping(value = "api/global/webssh", method = RequestMethod.DELETE)
    public HttpResponseTemp<?> deleteWebsshSetting() {
        long userId = AuthUtil.getUserId();
        return webConsoleService.deleteWebsshSetting(userId);
    }
}
