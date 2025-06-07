package dev.projectenhanced;

import dev.projectenhanced.enhancedspigot.util.rest.client.BodyBuilder;
import dev.projectenhanced.enhancedspigot.util.rest.client.HttpMethod;
import dev.projectenhanced.enhancedspigot.util.rest.client.EnhancedResponse;
import dev.projectenhanced.enhancedspigot.util.rest.client.EnhancedRequest;
import dev.projectenhanced.enhancedspigot.util.trycatch.TryCatchUtil;

import java.net.HttpCookie;

public class Main {
    public static void main(String[] args) {
        EnhancedResponse tokenResponse = TryCatchUtil.tryAndReturn(() -> EnhancedRequest.builder()
                .url("https://restful-booker.herokuapp.com/auth")
                .method(HttpMethod.POST)
                .body(
                        EnhancedRequest.Body.builder()
                                .set("username", "admin")
                                .set("password", "password123")
                )
                .build()
                .send());

        String token = tokenResponse.getAsJson().get("token").getAsString();

        EnhancedResponse create = TryCatchUtil.tryAndReturn(() -> EnhancedRequest.builder()
                .url("https://restful-booker.herokuapp.com/booking")
                .method(HttpMethod.POST)
                .body(
                        EnhancedRequest.Body.builder()
                                .set("firstname", "Mike")
                                .set("lastname", "Wyzinsky")
                                .set("totalprice", 10)
                                .set("depositpaid", true)
                                .set("bookingdates", BodyBuilder.builder()
                                        .set("checkin", "2025-01-01")
                                        .set("checkout", "2026-01-01")
                                )
                                .set("additionalneeds", "glass of wine every hour")
                )
                .build()
                .send()
        );

        String id = create.getAsJson().get("bookingid").getAsString();

        EnhancedResponse put = TryCatchUtil.tryAndReturn(() -> EnhancedRequest.builder()
                .url("https://restful-booker.herokuapp.com/booking/"+id)
                .method(HttpMethod.PUT)
                .cookie(new HttpCookie("token", token))
                .body(
                        EnhancedRequest.Body.builder()
                                .set("firstname", "Mike")
                                .set("lastname", "Wizinski")
                                .set("totalprice", 10)
                                .set("depositpaid", true)
                                .set("bookingdates", BodyBuilder.builder()
                                        .set("checkin", "2025-01-01")
                                        .set("checkout", "2026-01-01")
                                )
                                .set("additionalneeds", "glass of wine every hour")
                )
                .build()
                .send()
        );

        EnhancedResponse patch = TryCatchUtil.tryAndReturn(() -> EnhancedRequest.builder()
                .url("https://restful-booker.herokuapp.com/booking/"+id)
                .method(HttpMethod.PATCH)
                .cookie(new HttpCookie("token", token))
                .body(
                        EnhancedRequest.Body.builder()
                                .set("firstname", "Mikeie")
                )
                .build()
                .send()
        );

        EnhancedResponse delete = TryCatchUtil.tryAndReturn(() -> EnhancedRequest.builder()
                .url("https://restful-booker.herokuapp.com/booking/"+id)
                .method(HttpMethod.DELETE)
                .cookie(new HttpCookie("token", token))
                .build()
                .send()
        );

        EnhancedResponse visible = delete;
        System.out.println(visible.getStatus());
        System.out.println(visible.getMessage());
        System.out.println(visible.getAsString());
    }
}