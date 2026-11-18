package org.example.killer.controller;

import org.example.killer.dto.DragonDto;
import org.example.killer.service.DragonSearchService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/killer/dragon", produces = MediaType.APPLICATION_XML_VALUE)
public class DragonSearchController {

    private final DragonSearchService service;

    public DragonSearchController(DragonSearchService service) {
        this.service = service;
    }

    @GetMapping("/find-by-cave-depth/{max}")
    public DragonDto findByCaveDepth(@PathVariable("max") String max,
                                     @RequestParam(name = "mode", required = false) String mode) {
        return service.findByCaveDepth(RequestParams.longValue("max", max), RequestParams.caveMode(mode));
    }
}
