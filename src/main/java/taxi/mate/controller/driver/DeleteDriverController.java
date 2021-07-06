package taxi.mate.controller.driver;

import mate.lib.Injector;
import mate.service.DriverService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DeleteDriverController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(DeleteDriverController.class);
    private static final Injector injector = Injector.getInstance("mate");
    private final DriverService driverService =
            (DriverService) injector.getInstance(DriverService.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        logger.info("Method doGet in DeleteDriverController was called");
        driverService.delete(Long.parseLong(req.getParameter("id")));
        resp.sendRedirect("/drivers/all");
    }
}
