package milovanov.stc31.innopolis.checkuper.controller;

import milovanov.stc31.innopolis.checkuper.pojo.*;
import milovanov.stc31.innopolis.checkuper.service.IExecutorService;
import milovanov.stc31.innopolis.checkuper.service.IRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/requests")
public class RequestController {
    final private static Logger logger = LoggerFactory.getLogger(RequestController.class);
    final private IRequestService requestService;
    final private IExecutorService executorService;

    @Autowired
    public RequestController(IRequestService requestService, IExecutorService executorService) {
        this.requestService = requestService;
        this.executorService = executorService;
    }

    /**
     * Просмотр списка всех заказов для указанного исполнителя, если он указан, иначе список всех заказов всех исполнителей.
     *
     * @return ModelAndView с информацией по заказам и страницей отображения для указанного исполнителя
     */
    @GetMapping("/all")
    public java.lang.String getAllNotClosedRequests(Model model) {
        model.addAttribute("requests", requestService.getAllNotDoneRequests());
        return "/admin/applications";
    }

    /**
     * Просмотр списка всех заказов для указанного исполнителя, если он указан, иначе список всех заказов всех исполнителей.
     *
     * @return ModelAndView с информацией по заказам и страницей отображения для указанного исполнителя
     */
    @GetMapping("/closed")
    public java.lang.String getAllClosedRequests(Model model) {
        model.addAttribute("requests", requestService.getAllClosedRequests());
        return "/admin/history";
    }

    /**
     * Просмотр указанного в <tt>request_id</tt> заказа.
     *
     * @param paramRequestId идентификатор исполнителя
     * @return ModelAndView с информацией по заказу и страницей отображения
     */
    @GetMapping("/show")
    public ModelAndView getRequest(@RequestParam(value = "id", required = false) String paramRequestId) {
        Request request;
        java.lang.String title;
        try {
            Long requestId = Long.valueOf(paramRequestId);
            request = requestService.getRequestById(requestId);
            if (request == null) {
                title = "Заказ не найден";
            } else {
                title = request.getName();
            }
        } catch (NumberFormatException exception) {
            request = null;
            title = "Заказ не найден";
        }
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("title", title);
        modelAndView.addObject("request", request);
        modelAndView.setViewName("show_request");
        return modelAndView;
    }

    @GetMapping("/take")
    public ModelAndView takeRequest(@RequestParam(value = "id", required = false) String paramRequestId) {
        Request request;
        java.lang.String title;
        try {
            Long requestId = Long.valueOf(paramRequestId);
            request = requestService.getRequestById(requestId);
            if (request == null) {
                title = "Заказ не найден";
            } else {
                title = request.getName();
            }
        } catch (NumberFormatException exception) {
            request = null;
            title = "Заказ не найден";
        }
        Executor executor = executorService.getExecutorById(1001L);
        requestService.takeExecutor(request, executor);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("title", title);
        modelAndView.addObject("request", request);

        modelAndView.setViewName("show_request");
        return modelAndView;
    }

    /**
     * Просмотр списка всех заказов, которые я создал
     *
     * @return ModelAndView с информацией по заказам и страницей отображения
     */
    @GetMapping(value = "/my")
    public ModelAndView getRequestsCreatedMe(@AuthenticationPrincipal User user) {
        Customer customer = user.getCustomer();
        ModelAndView modelAndView =
                getModelAndView(requestService.getAllRequestsByCustomer(customer)
                        , "Мои заявки", "my"
                        , "user/workspase_applications");
        return modelAndView;
    }

    /**
     * Просмотр списка всех доступных для выполнения заказов
     *
     * @return ModelAndView с информацией по заказам и страницей отображения
     */
    @GetMapping(value = "/available")
    public ModelAndView getAllAvailableRequests() {
        ModelAndView modelAndView =
                getModelAndView(requestService.getAllAvailableRequests()
                        , "Доступные заявки", "available"
                        , "user/workspase_applications_select");
        return modelAndView;
    }

    /**
     * Просмотр списка всех заказов, находящихся у меня в работе
     *
     * @return ModelAndView с информацией по заказам и страницей отображения
     */
    @GetMapping(value = "/progress")
    public ModelAndView getMyRequestsInProgress(@AuthenticationPrincipal User user) {
        Executor executor = user.getExecutor();
        ModelAndView modelAndView =
                getModelAndView(requestService.getAllRequestsByExecutor(executor)
                        , "Доступные заявки", "progress"
                        , "user/workspase_applications_progress");
        return modelAndView;
    }

    /**
     * Удаление указанного в <tt>request_id</tt> заказа.
     *
     * @param paramRequestId идентификатор удаляемой заявки
     * @return ModelAndView с информацией по заказу и страницей отображения
     */
    @GetMapping("/delete")
    public String deleteRequest(@RequestParam(value = "id", required = false) String paramRequestId) {
        try {
            Long requestId = Long.valueOf(paramRequestId);
            requestService.deleteRequestById(requestId);
        } catch (NumberFormatException exception) {
            return "redirect:/errors/error";
        }
        return "redirect:/requests/my";
    }

    @GetMapping(value = "/addrequest")
    public String getRequestForAdd(Model model) {
//        Request request = new Request();
//        request.setTaskList(new ArrayList<>());
        model.addAttribute("request", new Request());
        model.addAttribute("COUNTROW", 3);
        model.addAttribute("task", new String[]{"111", "222", "333"});
        return "/user/workspase_applications_edit";
    }

    @GetMapping(value = "/editrequest")
    public String getRequestForEdit(Model model, @RequestParam(value = "reguestId", required = false) String paramRequestId) {
        try {
            model.addAttribute("request", requestService.getRequestById(Long.valueOf(paramRequestId)));
        } catch (NumberFormatException exception) {
            logger.error("Ошибка при разборе параметра reguestId = '{}'", paramRequestId, exception);
        }
        return "/user/workspase_applications_edit";
    }

    @PostMapping(value = "/addrequest")
    public String createRequest(
            @ModelAttribute Request request,
//            @Valid Task task,
            String[] task,
            @AuthenticationPrincipal User user) {

        request.setCustomer(user.getCustomer());
//        request.setStatus(RequestStatus.TODO);
//        requestDao.save(request);
//
//        String[] arrStr = task.getInfo().split(",");
//        for (String info : arrStr) {
//            task.setInfo(info);
//            task.setRequest(request);
//            taskDao.save(task);
//        }
        return "redirect:/requests/my";
    }

    /**
     * Возвращает ModelAndView с установленными необходимыми значениями
     *
     * @param requestList список заявок
     * @param title       название закладки
     * @param viewType    название элемента меню, который должен быть выбран
     * @param viewName    название html
     * @return ModelAndView с установленными необходимыми значениями
     */
    private ModelAndView getModelAndView(List<Request> requestList, String title, String viewType, String viewName) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("title", title);
        modelAndView.addObject("requests", requestList);
        modelAndView.addObject("viewType", viewType);
        modelAndView.setViewName(viewName);
        return modelAndView;
    }
}
