package com.testtaskbot.service;

import com.testtaskbot.model.entity.AppUser;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
@Service
public class DocumentService {

    /**
     * Генерирует Word-документ, содержащий информацию о пользователе.
     *
     * @param userData - объект с данными пользователя
     * @return файл, содержащий данные пользователя
     */
    public File generateDocWithUserInfo(AppUser userData) {
        File file = null;
        try {
            XWPFDocument document = new XWPFDocument();

            // Добавляем информацию о пользователе
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText("User Name: " + userData.getName());
            run.addBreak();
            run.setText("Surname: " + userData.getSurname());
            run.addBreak();
            run.setText("Sex: " + userData.getSex().toString());
            run.addBreak();
            run.setText("Birthday: " + userData.getBirthday());
            run.addBreak();

            // Сохраняем документ в файл
            file = new File("userDataInfo.docx");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                document.write(fos);
            }
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }
}
