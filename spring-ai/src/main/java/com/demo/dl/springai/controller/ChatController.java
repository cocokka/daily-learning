package com.demo.dl.springai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
class ChatController {

  private final OpenAiChatModel chatModel;

  private final ChatClient chatClient;

  @GetMapping("/ai")
  String generation(@RequestParam("message") String message) {
    return this.chatModel.call(message);
  }

  @GetMapping("/movie/composer")
  String chatMovieComposer(@RequestParam("composer") String composer) {
    return this.chatClient
        .prompt()
        .user(
            u ->
                u.text("Tell me the names of 5 movies whose soundtrack was composed by <composer>")
                    .param("composer", composer))
        .templateRenderer(
            StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
        .call()
        .content();
  }

  @GetMapping(value = "/streamChat", produces = "text/event-stream;charset=utf-8")
  Flux<String> streamChat(@RequestParam("message") String message, String language) {
    return this.chatClient
        .prompt()
        .user(
            u ->
                u.text("You are a helpful assistant, major in ${language}")
                    .param("language", language)
                    .text(message))
        .stream()
        .content();
  }
}
