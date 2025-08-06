package com.hdu.contorller;

import com.hdu.result.Result;
import com.hdu.service.PortraitService;
import com.hdu.entity.FlowImage;
import com.hdu.vo.UserPortraitsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/Portrait")
@RequiredArgsConstructor
@Tag(name = "用户画像相关接口")
public class PortraitContorller {
    private final PortraitService portraitService;


    @GetMapping("/{id}")
    @Operation(summary = "根据学号对学生画像")
    public Result<UserPortraitsVO> getPortrait(@PathVariable("id") String adslAccount) {
        log.info("根据学号: {} 对学生画像", adslAccount);
        return Result.success(portraitService.getPortrait(adslAccount));
    }


}
