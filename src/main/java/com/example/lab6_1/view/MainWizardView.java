package com.example.lab6_1.view;

import com.example.lab6_1.pojo.Wizard;
import com.example.lab6_1.pojo.Wizards;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;


@Route(value = "/mainPage.it")
public class MainWizardView extends VerticalLayout {
    private TextField fullname, dollars;
    private RadioButtonGroup<String> gender;
    private ComboBox position, school, house;
    private Button btnBefore, btnCreate, btnUpdate, btnDel, btnAfter;
    private HorizontalLayout h;
    private Notification noti;
    private Wizards wizards;
    private int index = 0;

    public MainWizardView() {
        wizards = new Wizards();
        fullname = new TextField();
        fullname.setPlaceholder("Fullname");
        gender = new RadioButtonGroup<>();
        gender.setLabel("Gender :");
        gender.setItems("Male", "Female");
        position = new ComboBox();
        position.setPlaceholder("Position");
        position.setItems("Student", "Teacher");
        dollars = new TextField("Dollars");
        dollars.setPrefixComponent(new Span("$"));
        school = new ComboBox();
        school.setPlaceholder("School");
        school.setItems("Hogwarts", "Beauxbatons", "Durmstrang");
        house = new ComboBox();
        house.setPlaceholder("House");
        house.setItems("Gryffindor", "Ravenclaw", "Hufflepuff", "Slyther");
        btnBefore = new Button("<<");
        btnCreate = new Button("Create");
        btnUpdate = new Button("Update");
        btnDel = new Button("Delete");
        btnAfter = new Button(">>");
        h = new HorizontalLayout();
        h.add(btnBefore, btnCreate, btnUpdate, btnDel, btnAfter);
        add(fullname, gender, position, dollars, school, house,h);

        this.fetchData();

        btnCreate.addClickListener(event -> {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("sex",  gender.getValue().equals("Male") ? "m" : "f");
            formData.add("name", fullname.getValue());
            formData.add("school", school.getValue().toString());
            formData.add("house", house.getValue().toString());
            formData.add("money", dollars.getValue());
            formData.add("position", position.getValue().toString());

            String out = WebClient.create()
                    .post()
                    .uri("http://localhost:8080/addWizard")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            Notification noti = Notification.show(out);
            this.fetchData();
            this.onTimeData();
        });

        btnUpdate.addClickListener(event -> {
            String sex = gender.getValue().equals("Male") ? "m" : "f";
            String name = fullname.getValue();
            String sc = school.getValue().toString();
            String ho = house.getValue().toString();
            int money = Integer.parseInt(dollars.getValue());
            String posi = position.getValue().toString();
            Wizard update = new Wizard(wizards.getModel().get(index).get_id(), sex, name, sc, ho, money, posi);

            String out = WebClient.create()
                    .post()
                    .uri("http://localhost:8080/updateWizard")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(update), Wizard.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            Notification noti = Notification.show(out);
            this.fetchData();
            this.onTimeData();
        });

        btnDel.addClickListener(event -> {
            String out = WebClient.create()
                    .post()
                    .uri("http://localhost:8080/deleteWizard")
                    .body(Mono.just(wizards.getModel().get(index)), Wizard.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            Notification noti = Notification.show(out);
            this.index = this.index != 0 ? this.index-1 : this.index+1;
            this.fetchData();
            this.onTimeData();
        });


        btnBefore.addClickListener(event -> {
            if (index == 0){
                index = 0;
            }
            else{
                index = index - 1;
            }
            this.onTimeData();
        });

        btnAfter.addClickListener(event -> {
            if (index == wizards.getModel().size()-1){
                index = wizards.getModel().size()-1;
            }
            else{
                index = index + 1;
            }
            this.onTimeData();
        });
    }
    private void fetchData(){
        ArrayList<Wizard> allWizards = WebClient.create()
                .get()
                .uri("http://localhost:8080/wizards")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ArrayList<Wizard>>() {})
                .block();
        wizards.setModel(allWizards);
    }

    //Show Detail Wizard
    private void onTimeData(){
        if (wizards.getModel().size() != 0){
            this.fullname.setValue(wizards.getModel().get(index).getName());
            this.gender.setValue(wizards.getModel().get(index).getSex().equals("m") ? "Male" : "Female");
            this.position.setValue(wizards.getModel().get(index).getPosition().equals("teacher") ? "Teacher" : "Student");
            this.dollars.setValue(String.valueOf(wizards.getModel().get(index).getMoney()));
            this.school.setValue(wizards.getModel().get(index).getSchool());
            this.house.setValue(wizards.getModel().get(index).getHouse());
        }
        else{
            this.fullname.setValue("");
            this.gender.setValue("");
            this.position.setValue("");
            this.dollars.setValue("");
            this.school.setValue("");
            this.house.setValue("");
        }
    }
}
