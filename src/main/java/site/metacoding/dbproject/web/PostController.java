package site.metacoding.dbproject.web;

import javax.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import site.metacoding.dbproject.domain.post.Post;
import site.metacoding.dbproject.domain.user.User;
import site.metacoding.dbproject.service.PostService;
import site.metacoding.dbproject.web.dto.ResponseDto;

@RequiredArgsConstructor // final이 붙은 애들에 대한 생성자를 만들어 준다.
@Controller
public class PostController {

    private final HttpSession session;
    private final PostService postService;

    // GET 글쓰기 페이지 /post/writeForm - 인증 O
    @GetMapping("/s/post/writeForm")
    public String writeForm() {

        if (session.getAttribute("principal") == null) {
            return "redirect:/loginForm";
        }

        return "post/writeForm";
    }

    // 메인페이지 (보통 주소가 두개다.) - 인증 X
    // 글목록 페이지 /post/list, /
    // @GetMapping({ "/", "/post/list" })
    @GetMapping({ "/", "/post/list" })
    public String list(@RequestParam(defaultValue = "0") Integer page, Model model) {
        // 1. postRepository의 findAll() 호출
        // 2. model에 담기
        // model.addAttribute("posts",
        // postRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));

        Page<Post> pagePosts = postService.글목록보기(page);

        model.addAttribute("posts", pagePosts);
        model.addAttribute("prevPage", page - 1);
        model.addAttribute("nextPage", page + 1);

        return "post/list";
    }

    // 글 상세보기 페이지 /post/{id} (삭제버튼 만들어 두면 됨, 수정버튼 만들어 두면 됨)
    // 인증 X
    @GetMapping("/post/{id}") // Get 요청에 /post 제외 시키기
    public String detail(@PathVariable Integer id, Model model) {

        User principal = (User) session.getAttribute("principal");

        Post postEntity = postService.글상세보기(id); // 핵심로직

        // 게시물이 없으면 error 페이지 이동
        if (postEntity == null) {
            return "error/page1";
        }

        if (principal != null) {
            // 권한 확인해서 view로 값 넘김
            if (principal.getId() == postEntity.getUser().getId()) { // 권한이 있다는 뜻
                model.addAttribute("pageOwner", true);
            } else {
                model.addAttribute("pageOwner", false);
            }
        }

        // 자바스크립트 공격 막기
        String rawContent = postEntity.getContent();
        String encContent = rawContent
                .replaceAll("<script>", "&lt;script&gt;")
                .replaceAll("</script>", "&lt;script/&gt;");
        postEntity.setContent(encContent);

        model.addAttribute("post", postEntity); // 핵심로직
        return "post/detail";
    }

    // 글 수정 페이지 /post/{id}/updateForm - 인증 O
    @GetMapping("/s/post/{id}/updateForm")
    public String updateForm(@PathVariable Integer id, Model model) {

        // 1. 인증 체크
        User principal = (User) session.getAttribute("principal");
        if (principal == null) {
            return "error/page1";
        }

        // 2. 권한 체크
        Post postEntity = postService.글상세보기(id); // 핵심로직
        if (postEntity.getUser().getId() != principal.getId()) { // lazy 전략이라면 이때 select 됐을 것임.
            return "error/page1";
        }

        model.addAttribute("post", postEntity); // 핵심로직

        return "post/updateForm"; // ViewResolver 도움 받음.
    }

    // DELETE 글삭제 /post/{id} - 글목록으로 가기 - 인증 O
    @DeleteMapping("/s/post/{id}")
    public @ResponseBody ResponseDto<String> delete(@PathVariable Integer id) {

        // 인증과 권한체크
        // 1. 인증 (세션필요)
        User principal = (User) session.getAttribute("principal");

        if (principal == null) { // 로그인이 안됐다는 뜻
            return new ResponseDto<String>(-1, "로그인이 되지 않았습니다.", null);
        }

        // 2. 권한
        Post postEntity = postService.글상세보기(id);
        if (principal.getId() != postEntity.getUser().getId()) { // 권한이 없다는 뜻
            return new ResponseDto<String>(-1, "해당 글을 삭제할 권한이 없습니다.", null);
        }

        postService.글삭제하기(id); // 내부적으로 exception이 터지면 무조건 스택 트레이스를 리턴한다.

        return new ResponseDto<String>(1, "성공", null);
    }

    // UPDATE 글수정 /post/{id} - 글상세보기 페이지가기 - 인증 O
    @PutMapping("/s/post/{id}")
    public @ResponseBody ResponseDto<String> update(@PathVariable Integer id, @RequestBody Post post) {

        // 1. 인증 체크
        User principal = (User) session.getAttribute("principal");
        if (principal == null) {
            return new ResponseDto<String>(-1, "로그인이 되지 않았습니다.", null);
        }

        // 2. 권한 체크
        Post postEntity = postService.글상세보기(id); // 핵심로직
        if (postEntity.getUser().getId() != principal.getId()) { // lazy 전략이라면 이때 select 됐을 것임.
            return new ResponseDto<String>(-1, "해당 게시글을 수정할 권한이 없습니다.", null);
        }

        postService.글수정하기(post, id);

        return new ResponseDto<String>(1, "수정성공", null);
    }

    // POST 글쓰기 /post - 글목록으로 가기 - 인증 O
    @PostMapping("/s/post")
    public String write(Post post) {

        // title, content 1. null 검사, 2. 공백 검사, 3. 길이 검사 등등....

        if (session.getAttribute("principal") == null) {
            return "redirect:/loginForm";
        }

        User principal = (User) session.getAttribute("principal");
        postService.글쓰기(post, principal);

        return "redirect:/";
    }
}
