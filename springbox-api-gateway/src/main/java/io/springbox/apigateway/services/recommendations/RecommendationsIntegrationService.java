package io.springbox.apigateway.services.recommendations;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.contrib.javanica.command.ObservableResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.stereotype.Service;
import rx.Observable;

import java.util.List;

@Service
public class RecommendationsIntegrationService {

    @Autowired
    @LoadBalanced
    private OAuth2RestOperations restTemplate;

    @HystrixCommand(fallbackMethod = "stubRecommendations",
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "5000"),
                    @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
            })
    public Observable<List<Movie>> getRecommendations(final String mlId) {
        return new ObservableResult<List<Movie>>() {
            @Override
            public List<Movie> invoke() {
                ParameterizedTypeReference<List<Movie>> responseType = new ParameterizedTypeReference<List<Movie>>() {
                };
                return restTemplate.exchange("http://springbox-recommendations/recommendations/forMovie/{mlId}", HttpMethod.GET, null, responseType, mlId).getBody();
            }
        };
    }

    @HystrixCommand(fallbackMethod = "stubLikes")
    public Observable<Boolean> likes(final String userName, final String mlId) {
        return new ObservableResult<Boolean>() {
            @Override
            public Boolean invoke() {
                return restTemplate.getForObject("http://springbox-recommendations/does/{userName}/like/{mlId}", Boolean.class, userName, mlId);
            }
        };
    }

    @SuppressWarnings("unused")
    private List<Movie> stubRecommendations(final String mlId) {
        return null;
    }

    @SuppressWarnings("unused")
    private Boolean stubLikes(final String userName, final String mlId) {
        return false;
    }

}

