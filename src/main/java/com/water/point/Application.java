package com.water.point;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * This is the main entry class to this Project/Application.
 * It is a stand alone java class with a main method {@link #main(String[])}
 * 
 * @author Simon Njenga
 * @since 0.1
 */
@SpringBootApplication
public class Application implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private static final String URL = "https://raw.githubusercontent.com/onaio/ona-tech/master/data/water_points.json";    
    
    /**
     * Entry point to the Application.
     * 
     * @param args Command line arguments
     * @throws IOException If starting the application fails
     */
    public static void main(String args[]) throws IOException {
        SpringApplication.run(Application.class, URL);    	  	
    }

    @Override
    public void run(String... strings) throws Exception {
        if (this.areWeOnline()) {
            // Create a new RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();
            MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
            List<MediaType> supportedMediaTypes = new LinkedList<MediaType>(converter.getSupportedMediaTypes());
            MediaType mediaType = new MediaType("text", "plain", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET);

            supportedMediaTypes.add(mediaType);
            converter.setSupportedMediaTypes(supportedMediaTypes);
            restTemplate.getMessageConverters().add(converter);

            // Set the Accept header
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setAccept(Collections.singletonList(mediaType));
            HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

            // Make the HTTP GET request, marshaling the response from JSON to an array of Events
            ResponseEntity<WaterPoint[]> responseEntity = restTemplate.exchange(URL, HttpMethod.GET, requestEntity, WaterPoint[].class);
            WaterPoint[] waterPoints = responseEntity.getBody();

            List<WaterPoint> wPoints = Arrays.asList(waterPoints);
            this.processWaterPoint(wPoints);
        } else {
            LOG.info("No Internet connection, Please provide a connection!");
            throw new IllegalStateException("No Internet connection found, Please provide a connection!");
        }
    }
    
    /**
     * Process the list of JSON data in form of a {@link WaterPoint} object.
     * 
     * @param cities the list of {@link WaterPoint} to use
     * @throws IOException If there is a problem
     * @throws IOException, JSONException 
     */
    private void processWaterPoint(List<WaterPoint> wPoints) throws IOException, JSONException {
        if (wPoints.isEmpty()) {
            LOG.info("No matching results found for the water point!");
        } else {
            Double functioningYes = 0D, functioningNo = 0D;

            Set<String> villageNames = new HashSet<String>();
            
            Multimap<String, String> countsYes = ArrayListMultimap.create();
            Multimap<String, String> countsNo = ArrayListMultimap.create();
            
            Map<String, Integer> percentageMap = new HashMap<String, Integer>();

            OrderedJSONObject orderedObject = new OrderedJSONObject();
            OrderedJSONObject orderedPercentage = new OrderedJSONObject();
            OrderedJSONObject waterCount = new OrderedJSONObject();

            DecimalFormat decimalFormat = new DecimalFormat("####0");

            // Process the water points
            for (int i = 0; i < wPoints.size(); i++) {
                villageNames.add(wPoints.get(i).getCommunities_villages().toString());

                String isWaterFunctioning = wPoints.get(i).isWater_functioning();

                if (isWaterFunctioning.equals("yes")) {
                    countsYes.put(wPoints.get(i).getCommunities_villages().toString(), "yes");
                    functioningYes++;
                } else if (isWaterFunctioning.equals("no")) {
                    countsNo.put(wPoints.get(i).getCommunities_villages().toString(), "no");
                    functioningNo++;
                }
            }

            orderedObject.put("number_functional:", decimalFormat.format(functioningYes));

            Iterator<String> villageName = villageNames.iterator();

            for(int i = 0; i < villageNames.size(); i++) {
                String name = villageName.next();
                Collection<String> countYes = countsYes.get(name);
                Collection<String> countNO = countsNo.get(name);

                waterCount.put(name, countYes.size());
                percentageMap.put(name, Integer.valueOf(decimalFormat.format((countNO.size() / functioningNo) * 100)));
            }

            orderedObject.put("number_water_points:", waterCount);

            Iterator<String> iterator = this.sortByValues(percentageMap).keySet().iterator();

            while (iterator.hasNext()) {
                String key = iterator.next();
                Integer value = percentageMap.get(key);
                orderedPercentage.put(key, value + "%");
            }

            orderedObject.put("community_ranking:", orderedPercentage);

            LOG.info(orderedObject.toString());
            LOG.info("matching results found and processed for the water point!");
        }
    }

    /**
     * Java method to sort Map in Java by value e.g. HashMap or Hashtable
     * throw NullPointerException if Map contains null values
     * It also sort values even if they are duplicates
     */
    @SuppressWarnings("unchecked")
	private <K extends Comparable,V extends Comparable> Map<K,V> sortByValues(Map<K,V> map){
        final List<Map.Entry<K,V>> entries = new LinkedList<Map.Entry<K,V>>(map.entrySet());

        final Comparator<Map.Entry<K,V>> COMPARE_PERCENTAGE = new Comparator<Map.Entry<K,V>>() {
        	@Override
            public int compare(Entry<K, V> o1, Entry<K, V> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
    	};
    	
        Collections.sort(entries, COMPARE_PERCENTAGE);
  
        //LinkedHashMap will keep the keys in the order they are inserted
        //which is currently sorted on natural ordering
        Map<K,V> sortedMap = new LinkedHashMap<K,V>();
  
        for(Map.Entry<K,V> entry: entries){
            sortedMap.put(entry.getKey(), entry.getValue());
        }  
        return sortedMap;
    }

    /**
     * Are we online? Do we have an Internet connection?
     * @return TRUE if we're online
     */
    private boolean areWeOnline() {
        boolean online = true;
        try {
            // test online connection with google.com worldwide home page
            new URL("http://www.google.com/").getContent();
        } catch (IOException ex) {
            online = false;
        }
        return online;
    }
}