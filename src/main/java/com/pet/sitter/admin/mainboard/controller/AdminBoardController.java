package com.pet.sitter.admin.mainboard.controller;

import com.pet.sitter.admin.mainboard.service.AdminBoardService;
import com.pet.sitter.admin.mainboard.validation.AdminBoardForm;
import com.pet.sitter.common.entity.Petsitter;
import com.pet.sitter.mainboard.dto.PetSitterDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RequestMapping("/admin")
@RequiredArgsConstructor
@Controller
public class AdminBoardController {

    private final AdminBoardService adminBoardService;

    //목록조회
    @GetMapping("/boardList")
    public String boardList(Model model, @RequestParam(value="page",defaultValue="0") int page,
                            Pageable pageable, String searchKeyword){
        Page<Petsitter> list = null;
        Page<PetSitterDTO> list2 = null;
        /*searchKeyword = 검색하는 단어*/
        if(searchKeyword == null){
            list2 = adminBoardService.getBoardList(pageable.getPageNumber()); //기존의 리스트보여줌
        }else{
            list = adminBoardService.boardSearchList(searchKeyword, pageable); //검색리스트반환
        }
        Page<PetSitterDTO> boardPage = adminBoardService.getBoardList(page);
        model.addAttribute("boardPage",boardPage);
        return "admin/boardList";
    }

    //상세조회
    @GetMapping("/detail/{sitterNo}")
    public String detail(@PathVariable("sitterNo") Long sitterNo,
                         Model model, AdminBoardForm adminCommentForm){
        PetSitterDTO boardDetail = adminBoardService.getBoardDetail(sitterNo);
        model.addAttribute("boardDetail",boardDetail);
        return "admin/boardDetail";
    }

    //수정폼
    @GetMapping("/modify/{sitterNo}")
    public String showModifyForm(AdminBoardForm adminBoardForm,
                                 @PathVariable("sitterNo") Long sitterNo, Principal principal, Model model) {
        PetSitterDTO petSitterDTO = adminBoardService.getBoardDetail(sitterNo);
        adminBoardForm.setPetTitle(petSitterDTO.getPetTitle());
        adminBoardForm.setPetContent(petSitterDTO.getPetContent());
        System.out.println("sitterNo = " + sitterNo);

        model.addAttribute("adminBoardForm", adminBoardForm);
        return "admin/boardForm"; // Return the form view
    }

    //수정처리
    @PostMapping("/modify/{sitterNo}")
    public String modify(@Valid AdminBoardForm adminBoardForm, BindingResult bindingResult,
                         @PathVariable("sitterNo") Long sitterNo, Principal principal){
        PetSitterDTO petSitterDTO = adminBoardService.getBoardDetail(sitterNo);
        //1.파라미터받기
        if(bindingResult.hasErrors()){
            return "admin/boardForm";
        }

        System.out.println(adminBoardForm.getPetTitle());
        //2.비즈니스로직수행
        //로그인한 유저가 글쓴이와 일치해야지만 수정권한을 가지게 된다 =>수정처리 진행된
        Petsitter petsitter = adminBoardService.getModify(sitterNo); //질문상세
        adminBoardService.modify(petsitter,adminBoardForm.getPetTitle(),adminBoardForm.getPetContent());
        return String.format("redirect:/admin/detail/%d",sitterNo); //수정상세페이지로 이동
    }

    //삭제
    @GetMapping("/delete/{sitterNo}")
    public String boardDelete(@PathVariable("sitterNo") Long sitterNo, Principal principal){
        adminBoardService.delete(sitterNo);
        return "redirect:/admin/boardList";
    }
}