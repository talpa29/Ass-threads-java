package bgu.spl.mics.application;

import bgu.spl.mics.Future;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.CPUService;
import bgu.spl.mics.application.services.ConferenceService;
import bgu.spl.mics.application.services.GPUService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.FileReader;
import bgu.spl.mics.application.objects.Student;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args) {
        System.out.println("heeeee");
        File input = new File("/home/daniel/IdeaProjects/assignment2/example_input.json");
        List<Student> Students =  new ArrayList<>();
        List<Model> Models =  new ArrayList<>();
        LinkedList<GPU> gpus =  new LinkedList<>();
        Vector<CPU> cpus = new Vector<>();
        List<ConfrenceInformation> confrenceInformations =  new ArrayList<>();
        try {
            JsonElement fileEle = JsonParser.parseReader(new FileReader(input));
            JsonObject fileObj = fileEle.getAsJsonObject();

            //extract tick and duration
            long tick = fileObj.get("TickTime").getAsLong();
            long duration = fileObj.get("Duration").getAsLong();

            //process all Students
            JsonArray JsonArrayOfStudent = fileObj.get("Students").getAsJsonArray();
            JsonArray JsonArrayOfGpu = fileObj.get("GPUS").getAsJsonArray();
            JsonArray JsonArrayOfCpu = fileObj.get("CPUS").getAsJsonArray();
            JsonArray JsonArrayOfConferance = fileObj.get("Conferences").getAsJsonArray();
            for (JsonElement StudentElement : JsonArrayOfStudent) {
                //get the Json Object
                JsonObject StudentJsonObject = StudentElement.getAsJsonObject();
                JsonArray JsonArrayOfModels = StudentJsonObject.get("models").getAsJsonArray();
                //extract data
                String name = StudentJsonObject.get("name").getAsString();
                String department = StudentJsonObject.get("department").getAsString();
                String status = StudentJsonObject.get("status").getAsString();
                Student student = new Student(name, department, status);
                for (JsonElement ModelsElement : JsonArrayOfModels) {
                    JsonObject ModelsJsonObjects = ModelsElement.getAsJsonObject();
                    //extract data
                    String ModelName = ModelsJsonObjects.get("name").getAsString();
                    String ModelType = ModelsJsonObjects.get("type").getAsString();
                    int ModelSize = ModelsJsonObjects.get("size").getAsInt();
                    Data data = new Data(ModelType, ModelSize, null);
                    Model model = new Model(student, data, ModelName);
                    data.setModel(model);
                    Students.add(student);
                    Models.add(model);
                }
            }
            for (JsonElement GpuElement : JsonArrayOfGpu) {
//                JsonObject GpuJsonObject = GpuElement.getAsJsonObject();
                String type = GpuElement.getAsString();
                GPU gpu = new GPU(type,null);
                gpus.add(gpu);
            }
            for (JsonElement CpuElement : JsonArrayOfCpu) {
//                JsonObject CpuJsonObject = CpuElement.getAsJsonObject();
                int NumOfCpus = CpuElement.getAsInt();
                CPU cpu = new CPU(NumOfCpus,null);
                cpus.add(cpu);
            }
            for (JsonElement ConferanceElement : JsonArrayOfConferance) {
                JsonObject ConferanceJsonObject = ConferanceElement.getAsJsonObject();
                String name = ConferanceJsonObject.get("name").getAsString();
                int date = ConferanceJsonObject.get("date").getAsInt();
                ConfrenceInformation confrenceInformation = new ConfrenceInformation(name,date);
                confrenceInformations.add(confrenceInformation);
            }


        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        Cluster cluster = Cluster.getInstance(gpus,cpus);
        Statistics statistics = new Statistics();
        int nam = 1;
        for (GPU gpu : gpus) {
            gpu.setCluster(cluster);
            GPUService gpuService = new GPUService("gpu"+ nam,gpu,statistics);
            nam++;
            Thread gpuT = new Thread(gpuService);
//          gpu.start();
        }
        nam = 1;
        for (CPU cpu : cpus) {
            cpu.setCluster(cluster);
            CPUService cpuService = new CPUService("cpu"+ nam,cpu,statistics);
            nam++;
            Thread cpuT = new Thread(cpuService);
//          cput.start();
        }
        TotalConferenceData total = new TotalConferenceData();
        for (ConfrenceInformation confrenceInformation: confrenceInformations) {
            ConferenceService conferenceService = new ConferenceService(confrenceInformation.getName(),confrenceInformation.getDate(),confrenceInformation,total);
            Thread conf = new Thread(conferenceService);
//          conf.start();
        }

    }
    }

