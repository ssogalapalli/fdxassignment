package com.fedexassignment.core.models;

import com.fedexassignment.core.beans.Feed;
import com.fedexassignment.core.parsers.RSSFeedParser;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;

/**
 * RSSFeed Model sling Model to read component configurations from dialog and render the feeds based on configured data from the RSSFeed component.
 */
@Model(adaptables = Resource.class)
public class RSSFeedModel {

    @ValueMapValue(name=PROPERTY_RESOURCE_TYPE, injectionStrategy= InjectionStrategy.OPTIONAL)
    @Default(values="No resourceType")
    protected String resourceType;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject @Optional
    private String feedURL;

    @Inject @Optional
    private String tobeReadFromRSS;

    @Inject @Optional
    private String noOfFeeds;

    @SlingObject
    private Resource currentResource;

    private int dispalyCount;

    @SlingObject
    private ResourceResolver resourceResolver;

    private List<Feed> feedList=null;
    @PostConstruct
    protected void init() {
        try{
            logger.info("RSS Feed Sling Model Logic starts here..");
            if(noOfFeeds!=null){
                dispalyCount=Integer.parseInt(noOfFeeds);
            }
            List<Feed> listOfFeeds= new ArrayList<Feed>();
            feedList= new ArrayList<Feed>();
            if(feedURL!=null){
                feedURL = this.feedURL;
                if(tobeReadFromRSS!=null && tobeReadFromRSS.equals("true")){
                    RSSFeedParser feedParser = new RSSFeedParser(feedURL);
                    logger.info("Read from Feed??...:{}",tobeReadFromRSS);
                    listOfFeeds = feedParser.readFeed();
                }else{
                    logger.info("Read From Component is True....");
                    Resource feedResource = resourceResolver.getResource(currentResource.getPath()+"/feeds");
                    if(feedResource!=null){
                        listOfFeeds=readFeedFromComponent(feedResource);
                    }

                }

                int i=0;
                for(Feed feed: listOfFeeds){
                    if(i<dispalyCount){
                        feedList.add(feed);
                        i++;
                    }

                 }
            }
        }catch(Exception e){
            logger.info("Exception in RSS Feed Model:{}",e.getMessage());
        }
        logger.info("RSS Feed Model Logic End here..");
    }

    /**
     * Method to read feeds data from multifield
     * @param componentResource
     * @return List of Feed Objects
     */
    public List<Feed> readFeedFromComponent(Resource componentResource){
       List<Feed> cfeedList = new ArrayList<Feed>();
        try{
            if(componentResource!=null){
                Iterator<Resource> childResources= componentResource.listChildren();
                if(childResources!=null)
                while(childResources.hasNext()){

                    Resource res= childResources.next();
                    if(res!=null){
                        String pubDate= res.getValueMap().get("feedPubDate", Date.class) !=null ?res.getValueMap().get("feedPubDate", Date.class).toString() : "";
                        Feed feed= null;
                        feed= new Feed(res.getValueMap().get("feedTitle","").toString(),res.getValueMap().
                                get("feedDescription","").toString(),pubDate);
                        cfeedList.add(feed);

                    }

                }
            }

        }catch (Exception e){
            logger.info("Exception while reading component Feed:",e.getMessage());
        }
        return cfeedList;
    }

    public String getFeedURL() {
        return feedURL;
    }

    public String isTobeReadFromRSS() {
        return tobeReadFromRSS;
    }

    public String getNoOfFeeds() {
        return noOfFeeds;
    }

    public List<Feed> getFeedList() {
        return feedList;
    }
    public int getDispalyCount() {
        return dispalyCount;
    }
}
