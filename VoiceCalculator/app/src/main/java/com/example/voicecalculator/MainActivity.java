package com.example.voicecalculator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.drawable.Icon;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;

    List<String> result = new ArrayList<String>();

    private TextView mainText;
    private TextView resultText;
    private ImageButton voiceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainText = findViewById(R.id.MainText);
        resultText = findViewById(R.id.ResultText);
        voiceButton = findViewById(R.id.VoiceButton);

        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak();
            }
        });
    }

    private List<String> getWordsInMessage(String message) {
        List<String> result = new ArrayList<String>();
        List<Character> symbolsInMessage = new ArrayList<Character>();

        for (int i = 0; i < message.length(); i++) {
            if (message.charAt(i) == '.')
                continue;

            if (message.charAt(i) == ' ') {
                result.add(getListCharToString(symbolsInMessage));
                symbolsInMessage.clear();
            }
            else if (message.charAt(i) == ',')
                symbolsInMessage.add('.');
            else
                symbolsInMessage.add(message.charAt(i));
        }

        result.add(getListCharToString(symbolsInMessage));
        return result;
    }

    private String getListCharToString(List<Character> symbolsInWord) {
        char[] masWithSymbols = new char[symbolsInWord.size()];

        for (int k = 0; k < masWithSymbols.length; k++)
            masWithSymbols[k] = symbolsInWord.get(k);

        return new String(masWithSymbols);
    }

    private void wordProcessing(String inputWord) {
        String[] operators = new String[] { "+", "-", "*", "/", };

        List<String[]> operatorWords = new ArrayList<String[]>();
        List<String[]> numList = new ArrayList<String[]>();

        operatorWords.add(new String[] { "слож", "плюс", "прибавь" });
        operatorWords.add(new String[] { "вычет", "вычти", "минус", "отними" });
        operatorWords.add(new String[] { "умножь", "х", "x", "X" });
        operatorWords.add(new String[] { "деление", "дели", "разделить" });

        numList.add(new String[] { "ноль", "нуль" });
        numList.add(new String[] { "один", "единица" });
        numList.add(new String[] { "два", "двойка" });
        numList.add(new String[] { "три", "тройка" });
        numList.add(new String[] { "четыре", "четвёрка" });
        numList.add(new String[] { "пять", "пятёрка" });
        numList.add(new String[] { "шесть", "шестёрка" });
        numList.add(new String[] { "семь", "семёрка" });
        numList.add(new String[] { "восемь", "восьмёрка" });
        numList.add(new String[] { "девять", "девятка" });

        if (isNumber(inputWord)) {
            result.add(inputWord);
            return;
        }

        for (int i = 0; i < operators.length; i++) {
            if (inputWord.equals(operators[i]) || isWordRecognized(inputWord, operatorWords.get(i))) {
                result.add(operators[i]);
                return;
            }
        }

        for (int i = 0; i < numList.size(); i++) {
            if (isWordRecognized(inputWord, numList.get(i))) {
                result.add(Integer.toString(i));
                return;
            }
        }
    }

    private boolean isWordRecognized(String yourWord, String[] words) {
        for (String word : words) {
            String yourCutWord = yourWord;
            int validSymbols = 0;

            if (yourWord.length() > word.length())
                yourCutWord = yourWord.substring(0, word.length());

            for (int i = 0; i < yourCutWord.length(); i++) {
                if (yourCutWord.charAt(i) == word.charAt(i))
                    validSymbols += 1;
            }

            if (validSymbols >= word.length() - (int)(word.length() / 3))
                return true;
        }

        return false;
    }

    private String eval(List<String> list) {
        float result = 0;
        boolean isListWithNumbers = false;

        for (int i = 0; i < list.size(); i++) {
            if (isNumber(list.get(i))) {
                result = Float.parseFloat(list.get(i));
                list.remove(i);
                isListWithNumbers = true;
                break;
            }
        }

        if (!isListWithNumbers)
            return "Ошибка! В сообщении не найдены числа!";

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals("+") || list.get(i).equals("-") || list.get(i).equals("*") || list.get(i).equals("/")) {
                for (int k = i + 1; k < list.size(); k++) {
                    if (isNumber(list.get(k))) {
                        switch (list.get(i)) {
                            case "+":
                                result += Float.parseFloat(list.get(k));
                                break;
                            case "-":
                                result -= Float.parseFloat(list.get(k));
                                break;
                            case "*":
                                result *= Float.parseFloat(list.get(k));
                                break;
                            case "/":
                                result /= Float.parseFloat(list.get(k));
                                break;
                        }
                    }
                    else
                        break;
                }
            }
        }

        return Float.toString(result);
    }

    private boolean isNumber(String message) {
        try {
            float intValue = Float.parseFloat(message);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    private void speak() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Скажите пример: ");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        }
        catch(Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                String message = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
                List<String> wordList = getWordsInMessage(message);

                for (String word : wordList) {
                    wordProcessing(word);
                }

                mainText.setText(String.format("Предложение разбито на: %s", wordList));
                resultText.setText("Ответ: " + eval(result));

                result.clear();
            }
        }
    }
}