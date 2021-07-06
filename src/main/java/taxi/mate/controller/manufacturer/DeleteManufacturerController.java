package taxi.mate.controller.manufacturer;

import mate.lib.Injector;
import mate.service.ManufacturerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DeleteManufacturerController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(DeleteManufacturerController.class);
    private static final Injector injector = Injector.getInstance("mate");
    private final ManufacturerService manufacturerService =
            (ManufacturerService) injector.getInstance(ManufacturerService.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        logger.info("Method doGet in DeleteManufacturerController was called");
        manufacturerService.delete(Long.parseLong(req.getParameter("id")));
        resp.sendRedirect("/manufacturers/all");
    }
}
