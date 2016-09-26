package com.jasonfunderburker.couchpotato.controller;

import com.jasonfunderburker.couchpotato.domain.TorrentUserInfo;
import com.jasonfunderburker.couchpotato.service.accounts.TorrentsAccountsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class AccountsController {
    private static final Logger logger = LoggerFactory.getLogger(AccountsController.class);
    @Autowired
    TorrentsAccountsService accountsService;

    @RequestMapping(value = "/torrentType/{id}/account", method = RequestMethod.POST)
    public String addTorrentAccount(@PathVariable("id")long id, TorrentUserInfo userInfo, ModelMap model) {
        userInfo.setTorrentTypeId(id);
        accountsService.addTorrentAccount(userInfo);
        return "redirect:/settings";
    }
}
