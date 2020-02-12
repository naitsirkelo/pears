package no.ntnu.pearproject;

import java.io.IOException;
import java.math.BigDecimal;

import javafx.fxml.FXML;

public class PrimaryController {

    @FXML
    private void initialize() {

        double n = sumNumbers(0, 100000000, 7, 5);

        System.out.printf("Sum: %.0f\n", n);
    }

    @FXML
    private void switchToSecondary() throws IOException {

        App.setRoot("secondary");
    }

    @FXML
    private double sumNumbers(int from, int to, int check, int avoid) {

        double sum = 0;

        for (int i = from; i <= to; i += check) {

            if (i % check == 0 && i % avoid != 0) {

                sum += i;
            }

        }

        return sum;

    }

}
