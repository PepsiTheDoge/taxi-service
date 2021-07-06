package taxi.mate.controller;

import mate.lib.Injector;
import mate.lib.exception.MyAuthenticationException;
import mate.model.Driver;
import mate.service.AuthenticationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LoginController extends HttpServlet {
    private static final String DRIVER_ID = "driver_id";
    private static final Logger logger = LogManager.getLogger(LoginController.class);
    private static final Injector injector = Injector.getInstance("mate");
    private final AuthenticationService authenticationService =
            (AuthenticationService) injector.getInstance(AuthenticationService.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        logger.info("Method doPost in LoginController was called");
        try {
            Driver driver = authenticationService.login(req.getParameter("login"),
                    req.getParameter("password"));
            HttpSession session = req.getSession();
            session.setAttribute(DRIVER_ID, driver.getId());
            resp.sendRedirect("/index");
        } catch (MyAuthenticationException e) {
            logger.error("A problem occurred in LoginController doPost method");
            req.setAttribute("errorMessage", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        }
    }
}
