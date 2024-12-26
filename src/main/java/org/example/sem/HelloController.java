package org.example.sem;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.text.Text;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.example.sem.sqlConnection.*;


public class HelloController {


    @FXML
    private TextField city;
    @FXML
    private Label description;
    @FXML
    private Label feels_like;


    @FXML
    private Label speed;
    @FXML
    private Label temp;
    @FXML
    private Label temp_max_min;


    @FXML
    private Label humidity;
    @FXML
    private Label pressure;
    @FXML
    private Label snow;

    @FXML
    private Label clouds;
    @FXML
    private ImageView icon;

    @FXML
    private Text firstDay;
    @FXML
    private Text secondDay;
    @FXML
    private Text thirdDay;
    @FXML
    private Text fourthDay;


    @FXML
    private Text firstDayTemp;
    @FXML
    private Text firstDayTempMaxMin;
    @FXML
    private Text fourthDayTemp;
    @FXML
    private Text fourthDayTempMaxMin;
    @FXML
    private Text secondDayTemp;
    @FXML
    private Text secondDayTempMaxMin;
    @FXML
    private Text thirdDayTemp;
    @FXML
    private Text thirdDayTempMaxMin;


    @FXML
    private ImageView iconFirstDay;
    @FXML
    private ImageView iconSecondDay;
    @FXML
    private ImageView iconThirdDay;
    @FXML
    private ImageView iconFourthDay;

    @FXML
    private Text savedCity;
    @FXML
    private Text errorMess;

    protected static final Logger logger = LoggerFactory.getLogger(HelloController.class);
    public void initialize() {
        cityFromDataBase();
    }


    protected void cityFromDataBase() {
        try {
            String str = savedCity();
            String output = getMainContent(str);
            logger.info("Из базы данных получено содержимое для города {}: {}", str, output);

            parseMainWeatherData(output, str);

            String outputForecast = getContentForecast(str);
            if (!outputForecast.isEmpty()) {
                parseWeatherDataForecast(outputForecast);

            }
        } catch (CityNotFoundException e) {
            logger.error("Ошибка: город не найден - {}", e.getMessage());
        }
    }


    @FXML
    protected void onSearch() {
        String getUserCity = city.getText().trim();
        logger.info("Пользователь ввел город: {}", getUserCity);
        try {
            String output = getMainContent(getUserCity);
            logger.info("Получено содержимое для города {}: {}", getUserCity, output);

            parseMainWeatherData(output, getUserCity);

            String outputForecast = getContentForecast(getUserCity);
            logger.info("Получен прогноз для города {}: {}", getUserCity, outputForecast);

            if (!outputForecast.isEmpty()) {
                parseWeatherDataForecast(outputForecast);
                dataBase(getUserCity);
            }
        } catch (CityNotFoundException e) {
            logger.error(e.getMessage());
            errorMess.setText("Введен неверный город. Попробуйте ещё раз ");
        } catch (ApiConnectionException e) {
            logger.error("Ошибка подключения к API: {}", e.getMessage());
        }
    }

    static String getMainContent(String city) throws CityNotFoundException {
        StringBuffer content = new StringBuffer();
        try {
            URL url = new URL("https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=b1b15e88fa797225412429c1c50c122a1&lang=ru");
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append("\n");
            }
            bufferedReader.close();
        } catch (IOException e) {
            logger.error("Ошибка получения информации для города {}: {}", city, e.getMessage());
            throw new CityNotFoundException("Введен неверный город: " + city);
        }
        return content.toString();
    }


    static String getContentForecast(String city) throws CityNotFoundException {
        StringBuffer content2 = new StringBuffer();
        try {
            URL url = new URL("https://api.openweathermap.org/data/2.5/forecast/daily?q=" + city + "&units=metric&lang=ru&cnt=5&appid=b1b15e88fa797225412429c1c50c122a1");
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content2.append(line).append("\n");
            }
            bufferedReader.close();
        } catch (IOException e) {
            logger.error("Ошибка получения прогноза для города {}: {}", city, e.getMessage());
            throw new CityNotFoundException("Введен неверный город: " + city);
        }
        return content2.toString();
    }

    private void parseMainWeatherData(String output, String getUserCity) {
        JSONObject obj = new JSONObject(output);
        if (!output.isEmpty()) {
            savedCity.setText(getUserCity);
            temp.setText((Math.round(obj.getJSONObject("main").getDouble("temp") - 273)) + "°C");
            temp_max_min.setText("макс.темп. " + (Math.round(obj.getJSONObject("main").getDouble("temp_max") - 273)+ "°C" + ", мин.темп. " + (Math.round(obj.getJSONObject("main").getDouble("temp_min") - 273)) + "°C"));
            feels_like.setText("Ощущается как: " + (Math.round(obj.getJSONObject("main").getDouble("feels_like") - 273)) + "°C");
            speed.setText((Math.round(obj.getJSONObject("wind").getDouble("speed"))) + " м/c");
            humidity.setText((obj.getJSONObject("main").getDouble("humidity")) + "%");
            pressure.setText(((obj.getJSONObject("main").getDouble("pressure")) * 0.75) + " мм.рт.ст.");
            description.setText((obj.getJSONArray("weather").getJSONObject(0).getString("description")));
            if (obj.has("snow")) {
                snow.setText((obj.getJSONObject("snow").getDouble("1h")) + " мм/ч");
            } else snow.setText("Осадков нет");
            String iconCode = obj.getJSONArray("weather").getJSONObject(0).getString("icon");
            String iconPath = "C:/Users/TigarSigar/IdeaProjects/sem-master/icons/" + iconCode + ".png";
            try {
                Image image = new Image(new FileInputStream(iconPath));
                icon.setImage(image);
            } catch (FileNotFoundException e) {
                logger.error("Иконка погоды не найдена: {}", e.getMessage());
            }
        }
    }

    private void parseWeatherDataForecast(String output) {
        JSONObject obj = new JSONObject(output);
        JSONArray list = obj.getJSONArray("list");

        for (int i = 1; i < list.length(); i++) {
            JSONObject dayData = list.getJSONObject(i);

            long unixTime = dayData.getLong("dt");
            // Преобразование Unix времени в Instant
            Instant instant = Instant.ofEpochSecond(unixTime);

            // Форматирование даты
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE-dd-MMM")
                    .withZone(ZoneId.systemDefault());

            String formattedDate = formatter.format(instant);
            double temperature = Math.round(dayData.getJSONObject("temp").getDouble("day"));
            double minTemperature = Math.round(dayData.getJSONObject("temp").getDouble("min"));
            double maxTemperature = Math.round(dayData.getJSONObject("temp").getDouble("max"));
            String description = dayData.getJSONArray("weather").getJSONObject(0).getString("description");
            String iconCode = dayData.getJSONArray("weather").getJSONObject(0).getString("icon");
            switch (i) {
                case 1:
                    firstDay.setText(formattedDate + '\n' + description);
                    firstDayTemp.setText(temperature + "°C ");
                    firstDayTempMaxMin.setText("мин." + minTemperature + '\n' + "макс." + maxTemperature);
                    setIcon(iconCode, iconFirstDay);
                    break;
                case 2:
                    secondDay.setText(formattedDate + '\n' + description);
                    secondDayTemp.setText(temperature + "°C ");
                    secondDayTempMaxMin.setText("мин." + minTemperature + '\n' + "макс." + maxTemperature);
                    setIcon(iconCode, iconSecondDay);
                    break;
                case 3:
                    thirdDay.setText(formattedDate + '\n' + description);
                    thirdDayTemp.setText(temperature + "°C ");
                    thirdDayTempMaxMin.setText("мин." + minTemperature + '\n' + "макс. " + maxTemperature);
                    setIcon(iconCode, iconThirdDay);
                    break;
                case 4:
                    fourthDay.setText(formattedDate + '\n' + description);
                    fourthDayTemp.setText(temperature + "°C ");
                    fourthDayTempMaxMin.setText("мин." + minTemperature + '\n' + "макс." + maxTemperature);
                    setIcon(iconCode, iconFourthDay);
                    break;
            }
        }
    }

    private void setIcon(String iconCode, ImageView imageView) {
        String iconPath = "C:/Users/TigarSigar/IdeaProjects/sem-master/icons/" + iconCode + ".png";
        try {
            Image image = new Image(new FileInputStream(iconPath));
            imageView.setImage(image);
        } catch (FileNotFoundException e) {
            logger.error("Иконка погоды не найдена: {}", e.getMessage());
        }
    }

}




