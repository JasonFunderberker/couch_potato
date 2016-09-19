package com.jasonfunderburker.couchpotato.service.check;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.jasonfunderburker.couchpotato.domain.TorrentItem;
import com.jasonfunderburker.couchpotato.domain.TorrentState;
import com.jasonfunderburker.couchpotato.domain.TorrentStatus;
import com.jasonfunderburker.couchpotato.exceptions.TorrentRetrieveException;
import com.jasonfunderburker.couchpotato.service.check.type.StateRetrieversDictionary;
import com.jasonfunderburker.couchpotato.service.check.type.TorrentRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by JasonFunderburker on 01.09.2016
 */
@Service
public class TorrentCheckServiceImpl implements TorrentCheckService {
    private static Logger logger = LoggerFactory.getLogger(TorrentCheckServiceImpl.class);

    @Override
    public void check(TorrentItem item) {
        try {
            final HtmlPage checkedPage;
            try (final WebClient webClient = new WebClient()) {
                webClient.getOptions().setJavaScriptEnabled(true);
                checkedPage = webClient.getPage(item.getLink());
            }
            logger.debug("response: {}", checkedPage);
            TorrentRetriever torrentRetriever = StateRetrieversDictionary.getRetrieverType(item.getType());
            if (torrentRetriever != null) {
                TorrentState newState = torrentRetriever.getState(checkedPage);
                if (item.getState() == null) {
                    item.setName(torrentRetriever.getName(checkedPage));
                    item.setStatus(TorrentStatus.NEW);
                    item.setState(newState);
                    item.setErrorText(null);
                }
                else {
                    if (!newState.equals(item.getState())) {
                        item.setStatus(TorrentStatus.REFRESHED);
                        item.setState(newState);
                        item.setErrorText(null);
                    } else {
                        item.setStatus(TorrentStatus.UNCHANGED);
                        item.setErrorText(null);
                    }
                }
            }
            else {
                item.setStatus(TorrentStatus.ERROR);
                item.setErrorText("Unsupported torrent type: "+ item.getType().getName());
            }
        }
        catch (MalformedURLException e) {
            item.setStatus(TorrentStatus.ERROR);
            item.setErrorText("Error create url from item link: "+item.getLink());
        }
        catch (IOException | FailingHttpStatusCodeException e) {
            item.setStatus(TorrentStatus.ERROR);
            item.setErrorText("Can't read response from url: "+item.getLink());
        }
        catch (TorrentRetrieveException e) {
            item.setStatus(TorrentStatus.ERROR);
            item.setErrorText(e.getMessage());
        }
    }
}

