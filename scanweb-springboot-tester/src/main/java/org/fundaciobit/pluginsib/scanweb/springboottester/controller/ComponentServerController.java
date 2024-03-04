package org.fundaciobit.pluginsib.scanweb.springboottester.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author anadal
 *
 */
@Controller
public class ComponentServerController {

    @Autowired
    private BuildProperties buildProperties;

    @RequestMapping(value = "/")
    public ModelAndView home() throws Exception {
        //response.setContentType("text/html");
        //response.getOutputStream().println("");
        
        ModelAndView model = new ModelAndView("index");
        return model;
        
        
    }

    /*
    @RequestMapping(value="/error")
    public String error() {
        return "<html><body><h1>ERROR XXXXX</h1></body></html>";
    }
    */

    @RequestMapping(value = "/public/versio")
    public void versio(HttpServletResponse response) throws Exception {

        response.getWriter().write(buildProperties.getVersion() + "|" + buildProperties.getTime());
        response.getWriter().flush();
        response.getWriter().close();

    }
}
