//package com.company;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.stream.*;

class Player {

    //LISTAS, MODULOS Y MOLECULAS

    private static final Diagnosis DIAGNOSIS = new Diagnosis();
    private static final Molecules MOLECULES = new Molecules();
    private static final Laboratory LABORATORY = new Laboratory();
    private static final Samples SAMPLES = new Samples();
    private static final tipoDeMolecula moleculaA = new tipoDeMolecula("A");
    private static final tipoDeMolecula moleculaB = new tipoDeMolecula("B");
    private static final tipoDeMolecula moleculaC = new tipoDeMolecula("C");
    private static final tipoDeMolecula moleculaD = new tipoDeMolecula("D");
    private static final tipoDeMolecula moleculaE = new tipoDeMolecula("E");
    private static final List<tipoDeMolecula> TIPOS_MOLECULAS = Arrays.asList(moleculaA, moleculaB, moleculaC, moleculaD, moleculaE);
    private static final List<SampleId> SAMPLE_TARGETS = new ArrayList<>();
    private static final Map<SampleId, Boolean> ESTADO_SAMPLE_IDENTIFICACION = new HashMap<>();
    public static final int maxRank = 2; // PROBANDO CON 3, NATURALMENTE 2

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int projectCount = in.nextInt();
        for (int i = 0; i < projectCount; i++) {
            int a = in.nextInt();
            int b = in.nextInt();
            int c = in.nextInt();
            int d = in.nextInt();
            int e = in.nextInt();
        }

        while (true) {
            AI rocheBot = buildAI(in);
            AI other = buildAI(in);
            int availableA = in.nextInt();
            int availableB = in.nextInt();
            int availableC = in.nextInt();
            int availableD = in.nextInt();
            int availableE = in.nextInt();
            int sampleCount = in.nextInt();
            List<Sample> samples = new ArrayList<>();
            for (int i = 0; i < sampleCount; i++) {
                /*
                int sampleId = in.nextInt();
                int carriedBy = in.nextInt();
                int rank = in.nextInt();
                String expertiseGain = in.next();
                int health = in.nextInt();
                int costA = in.nextInt();
                int costB = in.nextInt();
                int costC = in.nextInt();
                int costD = in.nextInt();
                int costE = in.nextInt();
                */
                samples.add(constructorSample(in));
            }
            artificialIntelligence(rocheBot, samples).run();
        }
    }

    //------ CLASES ------ CLASES ------ CLASES ------- CLASES ------- CLASES ------- CLASES ------//
// Inteligencia Artificial - aqui incia el AI
    private static Accion artificialIntelligence(AI rocheBot, List<Sample> samples) {
        if (samples.size() < 2 ) { // Checar que onda con el < 3
            return irASamples(rocheBot);
        }
        errorStream("Tengo los samples");

        if (SAMPLE_TARGETS.isEmpty()) {
            List<Sample> collect = samples.stream().filter(Sample::esMioOEstaEnLaNube).sorted(Comparator.comparing(Sample::costoTotalSalud).reversed()).collect(Collectors.toList());
            errorStream("collect => " + collect.size());
            SAMPLE_TARGETS.addAll(collect.subList(0, 1).stream().map(Sample::getSampleId).collect(Collectors.toList()));
        }
        errorStream("Samples en la mira");

        List<Sample> receta = samples.stream().filter(sample -> SAMPLE_TARGETS.contains(sample.getSampleId())).collect(Collectors.toList());
        errorStream(""+receta);
        rocheBot.dondeEstoy();

        if (receta.stream().anyMatch(Sample::noHaSidoDiagnosticado)) {
            return irADiagnosis(rocheBot, receta);
        }
        errorStream("TODO DIAGNOSTICADO");
        Map<tipoDeMolecula, Integer> cosasFaltantesPorRecolectar = rocheBot.getCosasFaltantes(receta);
        if (cosasFaltantesPorRecolectar.size() > 0) {
            return irAMoleculas(rocheBot, cosasFaltantesPorRecolectar);
        }
        errorStream("MOLECULAS HAN SIDO RECOLECTADAS");
        return irALaboratorio(rocheBot, receta);
    }


    private static void errorStream(String string) {
        System.err.println(string);
    }

    private static Accion irASamples(AI rocheBot) {
        if (rocheBot.at(SAMPLES)) {
            int rank = getRankAleatorio();
            return SAMPLES.conecta(rank);
        }
        return SAMPLES.veHacia();
    }

    private static Accion irALaboratorio(AI rocheBot, List<Sample> receta) {
        if (rocheBot.at(LABORATORY)) {
            SAMPLE_TARGETS.remove(0);
            Sample sample = receta.remove(0);
            return LABORATORY.conecta(sample.getSampleId());
        }
        return LABORATORY.veHacia();
    }

    private static Accion irAMoleculas(AI rocheBot, Map<tipoDeMolecula, Integer> cosasFaltantesPorRecolectar) {
        if (rocheBot.at(MOLECULES)) { //ARGREGAR && HAY ESPACIO EN LA MOCHILA
            return cosasFaltantesPorRecolectar.entrySet().stream().findFirst().map(Entry::getKey).map(MOLECULES::conecta).orElseThrow(IllegalStateException::new); // Agarra lo que le falta 
        }
        return MOLECULES.veHacia();
    }

    private static Accion irADiagnosis(AI rocheBot, List<Sample> receta) {
        if (rocheBot.at(DIAGNOSIS)) { /// MAYOR ES METODO POR DEFINIR PARA SAMPLE
            return receta.stream().filter(Sample::noHaSidoDiagnosticado).findFirst().map(Sample::getSampleId).map(DIAGNOSIS::conecta).orElseThrow(IllegalStateException::new); //SI NO HA SIDO DIAGNOSTICADO, LO HACE Y CONECTA
        }
        return DIAGNOSIS.veHacia();
    }

    private static int getRankAleatorio() { // Probar con un max rank de 3 restando el +1 en randomInt
        Random fortaleza = ThreadLocalRandom.current();
        int randomInt = fortaleza.nextInt(maxRank); // probando con 3
        return randomInt + 1; //return randomInt + 1;
    }

    private static Sample constructorSample(Scanner in) {
        int id = in.nextInt();
        int loTiene = in.nextInt();
        int rank = in.nextInt();
        String expertiseGain = in.next();
        int health = in.nextInt();
        Map<tipoDeMolecula, Integer> costoPorTipoDeMolecula = new HashMap<>();
        TIPOS_MOLECULAS.forEach(tipoDeMolecula -> costoPorTipoDeMolecula.put(tipoDeMolecula, in.nextInt()));
        SampleId sampleId = new SampleId(id);
        ESTADO_SAMPLE_IDENTIFICACION.putIfAbsent(sampleId, Boolean.FALSE);
        return new Sample(sampleId, loTiene, health, costoPorTipoDeMolecula);
    }

    protected static AI buildAI(Scanner in) {
        String target = in.next();
        int eta = in.nextInt();
        int score = in.nextInt();
        Map<tipoDeMolecula, Integer> almacenarPorTipoDeMolecula = new HashMap<>();
        TIPOS_MOLECULAS.forEach(tipoDeMolecula -> almacenarPorTipoDeMolecula.put(tipoDeMolecula, in.nextInt()));
        int expertiseA = in.nextInt();
        int expertiseB = in.nextInt();
        int expertiseC = in.nextInt();
        int expertiseD = in.nextInt();
        int expertiseE = in.nextInt();
        return new AI(target, almacenarPorTipoDeMolecula);
    }

    private static class AI {
        private final String target;
        private final Map<tipoDeMolecula, Integer> almacenarPorTipoDeMolecula;

        AI(String target, Map<tipoDeMolecula, Integer> almacenarPorTipoDeMolecula) {
            this.target = target;
            this.almacenarPorTipoDeMolecula = almacenarPorTipoDeMolecula;
        }

        boolean at(Module module) {
            return module.isRocheBot(target);
        }

        Map<tipoDeMolecula, Integer> getCosasFaltantes(List<Sample> receta) {
            Map<tipoDeMolecula, Integer> cosasFaltantes = new HashMap<>();
            TIPOS_MOLECULAS.forEach(tipoDeMolecula -> {
                Integer quantity = receta.stream().map(sample -> sample.costoPorTipoDeMolecula.get(tipoDeMolecula)).reduce((a, b) -> a + b).map(cost -> almacenarPorTipoDeMolecula.get(tipoDeMolecula) - cost).orElse(0);
                if (quantity < 0) {
                    cosasFaltantes.put(tipoDeMolecula, quantity);
                }
            });
            return cosasFaltantes;
        }

        void dondeEstoy() {
            errorStream("Reporta mi ubicación " + target);
        }

        String getDondeEstoy(){
            return target; // para poder evaluar
        }
    }


    /////////  *********** MODULO ************** //////////////
    private abstract static class Module {
        veHacia veHacia() {
            return new veHacia(getRocheBot());
        }

        protected abstract String getRocheBot();

        boolean isRocheBot(String target) {
            return target.equals(getRocheBot());
        }
    }
    /////////  *********** SampleId ************** //////////////
    private static class SampleId {
        private final int id;

        SampleId(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return Integer.toString(id);
        }

        @Override
        public boolean equals(Object objeto) {
            if (this==objeto)
                return true;
            if (objeto==null||getClass()!=objeto.getClass())
                return false;

            SampleId sampleId = (SampleId) objeto;

            return id == sampleId.id;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }
    /////////  *********** MODULO ************** //////////////
    private abstract static class Accion {
        abstract String getComando();

        void run() {
            System.out.println(getComando());
        }
    }
    /////////  *********** VE HACIA ************** //////////////
//////// ESTA SECCION DEL CODIGO SE ENCARGA DE
//////// MOSTRAR LA DIRECCION A LA CUAL SE TIENE QUE ACERCAR
//////// EL SHAVO ROBOT DE ROCHE
    private static class veHacia extends Accion {
        private final String aDondeVamos;

        veHacia(String aDondeVamos) {
            this.aDondeVamos = aDondeVamos;
        }

        @Override
        String getComando() {
            return "GOTO " + aDondeVamos;
        }
    }
    /////////  *********** DIAGNOSIS ************** ////////////// Datos sobre Diagnosis como Modulo
    private static class Diagnosis extends Module {
        @Override
        protected String getRocheBot() {
            return "DIAGNOSIS";
        }

        conecta conecta(SampleId sampleId) {
            ESTADO_SAMPLE_IDENTIFICACION.put(sampleId, Boolean.TRUE);
            return new conecta(sampleId.toString());
        }

    }
    ////////  *********** VE HACIA ************** ////////////// Se encarga de las conecciones en los modulos que requieren conecciones
    private static class conecta extends Accion {
        private final String parametroDeConeccion;

        @Override
        String getComando() {
            return "CONNECT " + parametroDeConeccion;
        }
        conecta(String parametroDeConeccion) {
            this.parametroDeConeccion = parametroDeConeccion;
        }


    }

    private static class Molecules extends Module {
        @Override
        protected String getRocheBot() {
            return "MOLECULES";
        }

        conecta conecta(tipoDeMolecula tipoDeMolecula) {
            return new conecta(tipoDeMolecula.toString());
        }
    }

    private static class tipoDeMolecula {
        private final String type;

        @Override
        public String toString() {
            return type;
        }

        tipoDeMolecula(String type) {
            this.type = type;
        }


    }

    private static class Laboratory extends Module {
        @Override
        protected String getRocheBot() {
            return "LABORATORY";
        }

        conecta conecta(SampleId sampleId) {
            ESTADO_SAMPLE_IDENTIFICACION.remove(sampleId);
            return new conecta(sampleId.toString());
        }
    }

    private static class Sample {
        private final int loTiene;
        private final int health;
        private final Map<tipoDeMolecula, Integer> costoPorTipoDeMolecula;
        private final SampleId sampleId;

        Sample(SampleId sampleId, int loTiene, int health, Map<tipoDeMolecula, Integer> costoPorTipoDeMolecula) {
            this.sampleId = sampleId;
            this.loTiene = loTiene;
            this.health = health;
            this.costoPorTipoDeMolecula = costoPorTipoDeMolecula;
        }

        boolean esMioOEstaEnLaNube() {
            return loTiene == 0 || estaEnLaNube();
        }

        double costoTotalSalud() {
            //double totalCostDouble = getTotalCost();
            return (double) health / getTotalCost();
        }

        private Integer getTotalCost() {
            return costoPorTipoDeMolecula.values().stream().reduce((a, b) -> a + b).orElse(0);
        }

        boolean estaEnLaNube() {
            return loTiene == -1;
        }

        SampleId getSampleId() {
            return sampleId;
        }

        boolean noHaSidoDiagnosticado() {
            return !ESTADO_SAMPLE_IDENTIFICACION.get(sampleId);
        }

        /*int mayor() {
            int maxValue = Integer.MIN_VALUE;
            for(int value : values.values()){
                if(value > maxValue){
                    maxValue = value;
                }
            } return maxValue;

                
                int maxValue = Integer.MIN_VALUE;
for (int value : values.values()) {
    if (value > maxValue) {
        maxValue = value;
    }
}
                
        }
        */

        @Override
        public String toString() {
            return  "costo de molecula por tipo=" + costoPorTipoDeMolecula + ", ID=" + sampleId ;
        }
    }



    private static class Samples extends Module {
        conecta conecta(int rank) {
            return new conecta(Integer.toString(rank));
        }

        @Override
        protected String getRocheBot() {
            return "SAMPLES";
        }
    }
}
