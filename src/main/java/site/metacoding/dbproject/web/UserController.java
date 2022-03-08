package site.metacoding.dbproject.web;

import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import site.metacoding.dbproject.domain.user.User;
import site.metacoding.dbproject.domain.user.UserRepository;

@Controller
public class UserController {

    // 컴퍼지션 (의존성 연결) 컨이 레한테 의존하고 있어
    private UserRepository userRepository;
    private HttpSession session;

    // DI 받는 코드!!
    // 적응되면 final + 롬북기능 사용하자
    public UserController(UserRepository userRepository, HttpSession session) {
        this.userRepository = userRepository;
        this.session = session;
    }

    // 회원가입 페이지 (정적) - 로그인X
    @GetMapping("/joinForm")
    public String joinForm() {
        return "user/joinForm";
    }

    // username=ssar&password=&email=ssar@nate.com 패스워드 공백
    // username=ssar&email=ssar@nate.com 패스워드 null
    // username=ssar&password=1234&email=ssar@nate.com (x-www-form)
    // 회원가입 (페이지가 아니라 액션을 주는 것 - 회원가입 수행) - 로그인X
    @PostMapping("/join")
    public String join(User user) {

        // 1. username, password, email - 1. null체크, 2. 공백체크
        if (user.getUsername() == null || user.getPassword() == null || user.getEmail() == null) {
            return "redirect:/joinForm";
        }
        if (user.getUsername().equals("") || user.getPassword().equals("") || user.getEmail().equals("")) {
            return "redirect:/joinForm";
        }

        // 2. 핵심로직
        User userEntity = userRepository.save(user);
        System.out.println("userEntity : " + userEntity);
        // redirect:매핑주소
        return "redirect:/loginForm"; // 로그인 페이지 이동해주는 컨트롤러 메서드 재활용
    }

    // 로그인 페이지 (정적) - 로그인X
    @GetMapping("/loginForm")
    public String loginForm() {
        return "user/loginForm";
    }

    // SELECT * FROM user WHERE username=? AND password=?
    // 원래 SELECT는 무조건 get 요청
    // 그런데 로그인만 예외 (POST)
    // 이유 : 주소에 패스워드를 남길 수 없으니까!!!
    // 로그인 수행 - 로그인X
    @PostMapping("/login")
    public String login(User user) {

        User userEntity = userRepository.mLogin(user.getUsername(), user.getPassword());

        if (userEntity == null) {
            System.out.println("아이디 혹은 패스워드가 틀렸습니다");
        } else {
            System.out.println("로그인 되었습니다.");
            session.setAttribute("principal", userEntity);
        }
        // 1. DB 연결해서 username, password 있는지 확인
        // 2. 있으면 session 영역에 인증됨 이라고 메시지 하나 넣어두자.
        return "redirect:/"; // PostController 만들고 수정하자.
    }

    // http://localhost:8080/user/1
    // 유저상세 페이지 (동적) - 로그인O
    @GetMapping("/user/{id}")
    public String detail(@PathVariable Integer id, Model model) {

        // 유효성 검사하기 (수십개...엄청 많음)
        User principal = (User) session.getAttribute("principal");

        // 1. 인증 체크
        if (principal == null) { // 로그인 인증해야 접속 가능하게 하기
            return "error/page1"; // 못 가게 해야됨
        }

        // 2. 권한 체크
        if (principal.getId() != id) {
            return "error/page1";
        }

        // 3. 핵심 로직
        Optional<User> userOp = userRepository.findById(id); // user 정보

        if (userOp.isPresent()) { // 박스 안에 선물이 있으면
            User userEntity = userOp.get();
            model.addAttribute("user", userEntity);
            return "user/detail";
        } else {
            // 경고창 같은게 있으면 좋다. (해당페이지로 이동할 수 없습니다.)
            return "error/page1"; // null이면 메인페이지로 리턴
        }

        // DB에 로그 남기기 (로그인 한 아이디)
    }

    // 유저수정 페이지 (동적) - 로그인O
    @GetMapping("/user/{id}/updateForm")
    public String updateForm(@PathVariable Integer id) {
        return "user/updateForm";
    }

    // 유저수정 - 로그인O
    @PutMapping("/user/{id}")
    public String update(@PathVariable Integer id) {
        return "redirect:/user/" + id;
    }

    // 로그아웃 - 로그인O
    @GetMapping("/logout")
    public String logout() {
        return "메인페이지를 돌려주면 됨"; // PostController 만들고 수정하자.
    }
}
