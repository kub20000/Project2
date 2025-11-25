package com.emergency.myPage.controllers;

import com.emergency.enrollment.domain.MyStudyCourseItem;
import com.emergency.enrollment.domain.MyStudyStatusCount;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.emergency.enrollment.service.EnrollmentService;
import com.emergency.user.web.LoginUser;
import java.util.List;

@Controller
public class MyPageController {

    private final EnrollmentService enrollmentService;

    public MyPageController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    private void setupMyPage(Model model, String title, String contentTemplate) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("activeMenu", "MYCLASS");
        model.addAttribute("showSidebar", true); // 나의 강의실 사이드바

        model.addAttribute("contentTemplate", contentTemplate);

        model.addAttribute("pageCss", List.of(
                "/css/join_css/join.css",
                "/css/myPage_css/myStudy.css",
                "/css/pages_css/news.css"
        ));
        model.addAttribute("pageJs", List.of("/fragments_js/pageNationC.JS","/fragments_js/inoModal.js"
        ,"/fragments_js/coursePopUp.js","/fragments_js/courseList.js"
        ));
    }

    @GetMapping({"/myPage/myStudy", "/myPage/myStudy.html"})
    public String myStudy(Model model,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {

        // ✅ 세션에서 LoginUser 꺼내기
        // LoginController 안에 public static final String LOGIN_USER = "loginUser"; 이런 상수 있으면 그걸 쓰는 게 제일 안전함
        LoginUser loginUser = (LoginUser) session.getAttribute("LOGIN_USER");
        // 또는:
        // LoginUser loginUser = (LoginUser) session.getAttribute(LoginController.LOGIN_USER);

        // 로그인 안 된 경우
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("loginMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/login";
        }

        // ✅ 로그인 유저의 id 꺼내기
        // LoginUser 클래스 안에 있는 getter 이름에 맞춰서 사용
        Long userId = loginUser.getUserId();   // getId()면 getId()로 바꿔줘야 함

        // ✅ 상태별 수강 현황 조회
        MyStudyStatusCount counts = enrollmentService.getMyStudyStatusCount(userId);

        model.addAttribute("studyingCount", counts.getStudyingCount());
        model.addAttribute("completedCount", counts.getCompletedCount());
        model.addAttribute("notCompletedCount", counts.getNotCompletedCount());

        // ✅ 강의 이어 보기 리스트
        List<MyStudyCourseItem> ongoingCourses =
                enrollmentService.getMyOngoingCoursesForMyStudy(userId);
        model.addAttribute("ongoingCourses", ongoingCourses);

        // 마이페이지 공통 세팅
        setupMyPage(model, "나의 학습활동", "myPage/myStudy");
        return "layout";
    }

    @GetMapping({"/myPage/question", "/myPage/question.html"})
    public String questionList(Model model) {
        // templates/pages/question.html
        setupMyPage(model, "문의사항", "myPage/question");
        return "layout";
    }

    @GetMapping({"/myPage/questionDetail", "/myPage/questionDetail.html"})
    public String questionDetail(Model model) {
        // templates/pages/questionDetail.html
        setupMyPage(model, "문의사항 상세", "myPage/questionDetail");
        return "layout";
    }

    @GetMapping({"/myPage/uploadQuestion", "/myPage/uploadQuestion.html"})
    public String uploadQuestion(Model model) {
        // templates/pages/uploadQuestion.html
        setupMyPage(model, "문의사항 등록", "myPage/uploadQuestion");
        return "layout";
    }

}
