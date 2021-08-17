package com.example.demo1.events;

import org.modelmapper.ModelMapper;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
public class EventController {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    public EventController( EventRepository eventRepository, ModelMapper modelMapper, EventValidator eventValidator){
        this.eventRepository = eventRepository;
        this.modelMapper = modelMapper;
        this.eventValidator = eventValidator;
    }

    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors){
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().body(errors);
        }

        eventValidator.validate(eventDto, errors);
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().body(errors);
        }

        //eventDto는 id값등이 없기때문에 test에서 입력했어도 값이 들어오지않음
        //modelMapper는 eventDto.setId(...)처럼 하나하나 넣어서 event로 만드는작업을 줄이고 한번에 변환해줌
        Event event = modelMapper.map(eventDto, Event.class);
        event.update();
        Event newEvent = this.eventRepository.save(event);
        ControllerLinkBuilder slash = linkTo(EventController.class).slash(newEvent.getId());
        URI createUri = slash.toUri();
        EventResource t = new EventResource(event);
        t.add(linkTo(EventController.class).withRel("query-events"));
        t.add(slash.withRel("update-event"));
        return ResponseEntity.created(createUri).body(t);
    }
}
