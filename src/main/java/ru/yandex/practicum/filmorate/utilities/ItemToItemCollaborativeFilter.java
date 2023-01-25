package ru.yandex.practicum.filmorate.utilities;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.random;
import static java.lang.Math.rint;

public class ItemToItemCollaborativeFilter {
    private static final Boolean TREAT_EMPTY_AS_ZEROS = true; // when rates != 1, false needed
    private static final Integer HOW_MANY_SIMILAR_ITEMS_TO_ANALYZE = 2; //internal, subject for test

    public static void main(String[] args) {
        Map<Integer, Map<Integer, Optional<Double>>> testMap = generateTestMap();
        Integer userId = 5;
        List<Integer> recommendations = getRecommendationsForUser(testMap, userId, Optional.empty());
    }

    public static List<Integer> getRecommendationsForUser(Map<Integer, Map<Integer, Optional<Double>>> matrix, Integer userId, Optional<Integer> limit) {
        if (matrix == null || userId == null) {
            return List.of();
        }
        List<Integer> recommendations = getRecommendationsInternal(matrix, userId);
        if (limit.isEmpty()) {
            return recommendations;
        } else {
            return recommendations.subList(0, Integer.min(limit.get(), recommendations.size()));
        }
    }

    private static List<Integer> getRecommendationsInternal(Map<Integer, Map<Integer, Optional<Double>>> matrix, Integer userId) {
        Map<Integer, Map<Integer, Double>> matrixForSimilarities = prepareMatrix(matrix);
        Map<Integer, Map<Integer, Double>> similarities = findSimilarities(matrixForSimilarities);
        Map<Integer, Double> absentRatings = new HashMap<>();
        for (Integer itemId : matrix.keySet()) {
            if (matrix.get(itemId).get(userId).isEmpty()) {
                Map<Integer, Double> mostSimilarPresentItems = getMostSimilarPresentItems(matrix, similarities, itemId, userId);
                Double absentRatingSum = mostSimilarPresentItems.keySet().stream()
                        .map(aDouble -> matrix.get(aDouble).get(userId).get() * mostSimilarPresentItems.get(aDouble))
                        .mapToDouble(e->e)
                        .sum();
                Double absentRatingDenom = mostSimilarPresentItems.values().stream().mapToDouble(e->e).sum();
                Double absentRating = absentRatingSum / absentRatingDenom;
                absentRatings.put(itemId, absentRating);
            }
        }
        return absentRatings.entrySet().stream()
                .sorted((e1,e2)->e2.getValue().compareTo(e1.getValue()))
                .collect(ArrayList::new,
                        (list,e) -> list.add(e.getKey()),
                        ArrayList::addAll);
    }

    private static Map<Integer, Double> getMostSimilarPresentItems(Map<Integer, Map<Integer, Optional<Double>>> matrix, Map<Integer, Map<Integer, Double>> similarities, Integer itemId, Integer userId) {
        Map<Integer, Double> presentItemsMap = similarities.get(itemId).entrySet().stream()
                .collect(
                        HashMap::new,
                        (map, e)-> {
                            if (matrix.get(e.getKey()).get(userId).isPresent()) { // item to compare is rated by user with userId
                                map.put(e.getKey(), e.getValue());
                            }
                        },
                        HashMap::putAll
                );
        List<Integer> mostSimilarItems = presentItemsMap.entrySet().stream()
                .sorted((e1,e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(ArrayList::new,
                        (list, e) -> list.add(e.getKey()),
                        ArrayList::addAll
                );
        Map<Integer, Double> result = new HashMap<>();
        for (Integer key : mostSimilarItems.subList(0, Integer.min(mostSimilarItems.size(), HOW_MANY_SIMILAR_ITEMS_TO_ANALYZE))) {
            result.put(key, presentItemsMap.get(key));
        }
        return result;
    }

    private static Map<Integer, Map<Integer, Double>> prepareMatrix(Map<Integer, Map<Integer, Optional<Double>>> matrix) {
        Map<Integer, Map<Integer, Optional<Double>>> preparedMatrix = new HashMap<>();

        for (Integer key : matrix.keySet()) {
            preparedMatrix.put(key, new HashMap<>());
            for (Integer key2 : matrix.get(key).keySet()) {
                preparedMatrix.get(key).put(key2, matrix.get(key).get(key2));
            }
        }

        if (ItemToItemCollaborativeFilter.TREAT_EMPTY_AS_ZEROS) {
            for (Integer key : preparedMatrix.keySet()) {
                for (Integer key2 : preparedMatrix.get(key).keySet()) {
                    if (preparedMatrix.get(key).get(key2).isEmpty()) {
                        preparedMatrix.get(key).put(key2, Optional.of((double) 0));
                    }
                }
            }
        }

        Map<Integer, Map<Integer, Double>> resultMatrix = new HashMap<>();

        for (Integer key : preparedMatrix.keySet()) {
            resultMatrix.put(key, new HashMap<>());
            for (Integer key2 : preparedMatrix.get(key).keySet()) {
                Map<Integer, Optional<Double>> column = preparedMatrix.entrySet().stream()
                        .collect(HashMap::new,
                                (map,e)->{
                                    for (Integer key3 : e.getValue().keySet()) {
                                        if (Objects.equals(key3, key2)) {
                                            map.put(e.getKey(), e.getValue().get(key2));
                                        }
                                    }
                                },
                                HashMap::putAll
                        );
                Double columnMean = findAverageValue(column);
                if (preparedMatrix.get(key).get(key2).isPresent()) {
                    Double value = preparedMatrix.get(key).get(key2).get() - columnMean;
                    resultMatrix.get(key).put(key2, value);
                } else {
                    Double value = (double) 0;
                    resultMatrix.get(key).put(key2, value);
                }
            }

        }


        return resultMatrix;
    }


    private static Map<Integer, Map<Integer, Double>> findSimilarities(Map<Integer, Map<Integer, Double>> preparedMatrix) {
        Map<Integer, Map<Integer, Double>> similarities = new HashMap<>();
        for (Integer key : preparedMatrix.keySet()) {
            similarities.put(key, new HashMap<>());
            for (Integer key2 : preparedMatrix.keySet()) {
                if (similarities.get(key2) != null && similarities.get(key2).get(key) != null) {
                    similarities.get(key).put(key2, similarities.get(key2).get(key));
                } else {
                    Map<Integer, Double> vector1 = preparedMatrix.get(key);
                    Map<Integer, Double> vector2 = preparedMatrix.get(key2);
                    similarities.get(key).put(key2, findCosineSimilarity(vector1, vector2));
                }
            }
        }
        return similarities;
    }


    //function accepts two vectors with non-null non-empty equal keys and double values
    private static Double findCosineSimilarity(Map<Integer, Double> vector1, Map<Integer, Double> vector2) {
        if (vector1 == null || vector2 == null) {
            return null;
        }
        if (vector1.isEmpty() || vector2.isEmpty()) {
            return null;
        }
        if (vector1.containsKey(null) || vector2.containsKey(null)) {
            return null;
        }
        if (!vector1.keySet().equals(vector2.keySet())) {
            return null;
        }
        Double lenOfVector1 = findVectorLength(vector1);
        Double lenOfVector2 = findVectorLength(vector2);
        Double scalarProductOfVectors = findScalarProductOfVectors(vector1, vector2);
        return scalarProductOfVectors / (lenOfVector1 * lenOfVector2);
    }

    private static Map<Integer, Double> subtractValueFromVectorWithNulls(Map<Integer, Optional<Double>> vector, Double value) {
        return vector.entrySet().stream()
                .collect(
                        HashMap::new,
                        (map, e) -> {
                            double result;
                            if (e.getValue().isEmpty()) {
                                result = 0;
                            } else {
                                result = e.getValue().get() - value;
                            }
                            map.put(e.getKey(), result);
                        },
                        HashMap::putAll
                );
    }

    private static Double findAverageValue(Map<Integer, Optional<Double>> map) {
        return map.values().stream()
                .filter(Optional::isPresent)
                .mapToDouble(Optional::get)
                .average()
                .getAsDouble();
    }

    private static Double findMatrixAverageValue(Map<Integer, Map<Integer, Optional<Double>>> matrix) {
        List<Optional<Double>> allValues = matrix.values().stream()
                .collect(ArrayList::new,
                        (list,e)->{
                            List<Optional<Double>> values = new ArrayList<>(e.values());
                            list.addAll(values);
                        },
                        ArrayList::addAll);
        return allValues.stream()
                .filter(Optional::isPresent)
                .mapToDouble(Optional::get)
                .average()
                .getAsDouble();
    }

    private static Double findVectorLength(Map<Integer, Double> vector) {
        return Math.sqrt(vector.values().stream().map(e->Math.pow(e,2)).mapToDouble(e->e).sum());
    }

    private static Double findScalarProductOfVectors(Map<Integer, Double> vector1, Map<Integer, Double> vector2) {
        return vector1.entrySet().stream()
                .map(e->e.getValue()*vector2.get(e.getKey()))
                .mapToDouble(e->e)
                .sum();
    }

    public static Map<Integer, Map<Integer, Optional<Double>>> generateTestMap() {
        Map<Integer, Map<Integer, Optional<Double>>> testMap = new HashMap<>();
        Map<Integer, Optional<Double>> row1 = new HashMap<>();
        Map<Integer, Optional<Double>> row2 = new HashMap<>();
        Map<Integer, Optional<Double>> row3 = new HashMap<>();
        Map<Integer, Optional<Double>> row4 = new HashMap<>();
        Map<Integer, Optional<Double>> row5 = new HashMap<>();
        Map<Integer, Optional<Double>> row6 = new HashMap<>();
        row1.put(1,Optional.of((double)1));
        row2.put(1,Optional.empty());
        row3.put(1,Optional.of((double)2));
        row4.put(1,Optional.empty());
        row5.put(1,Optional.empty());
        row6.put(1,Optional.of((double)1));
        row1.put(2,Optional.empty());
        row2.put(2,Optional.empty());
        row3.put(2,Optional.of((double)4));
        row4.put(2,Optional.of((double)2));
        row5.put(2,Optional.empty());
        row6.put(2,Optional.empty());
        row1.put(3,Optional.of((double)3));
        row2.put(3,Optional.of((double)5));
        row3.put(3,Optional.empty());
        row4.put(3,Optional.of((double)4));
        row5.put(3,Optional.of((double)4));
        row6.put(3,Optional.of((double)3));
        row1.put(4,Optional.empty());
        row2.put(4,Optional.of((double)4));
        row3.put(4,Optional.of((double)1));
        row4.put(4,Optional.empty());
        row5.put(4,Optional.of((double)3));
        row6.put(4,Optional.empty());
        row1.put(5,Optional.empty());
        row2.put(5,Optional.empty());
        row3.put(5,Optional.of((double)2));
        row4.put(5,Optional.of((double)5));
        row5.put(5,Optional.of((double)4));
        row6.put(5,Optional.of((double)3));
        row1.put(6,Optional.of((double)5));
        row2.put(6,Optional.empty());
        row3.put(6,Optional.empty());
        row4.put(6,Optional.empty());
        row5.put(6,Optional.of((double)2));
        row6.put(6,Optional.empty());
        row1.put(7,Optional.empty());
        row2.put(7,Optional.of((double)4));
        row3.put(7,Optional.of((double)3));
        row4.put(7,Optional.empty());
        row5.put(7,Optional.empty());
        row6.put(7,Optional.empty());
        row1.put(8,Optional.empty());
        row2.put(8,Optional.empty());
        row3.put(8,Optional.empty());
        row4.put(8,Optional.of((double)4));
        row5.put(8,Optional.empty());
        row6.put(8,Optional.of((double)2));
        row1.put(9,Optional.of((double)5));
        row2.put(9,Optional.empty());
        row3.put(9,Optional.of((double)4));
        row4.put(9,Optional.empty());
        row5.put(9,Optional.empty());
        row6.put(9,Optional.empty());
        row1.put(10,Optional.empty());
        row2.put(10,Optional.of((double)2));
        row3.put(10,Optional.of((double)3));
        row4.put(10,Optional.empty());
        row5.put(10,Optional.empty());
        row6.put(10,Optional.empty());
        row1.put(11,Optional.of((double)4));
        row2.put(11,Optional.of((double)1));
        row3.put(11,Optional.of((double)5));
        row4.put(11,Optional.of((double)2));
        row5.put(11,Optional.of((double)2));
        row6.put(11,Optional.of((double)4));
        row1.put(12,Optional.empty());
        row2.put(12,Optional.of((double)3));
        row3.put(12,Optional.empty());
        row4.put(12,Optional.empty());
        row5.put(12,Optional.of((double)5));
        row6.put(12,Optional.empty());
        testMap.put(1,row1);
        testMap.put(2,row2);
        testMap.put(3,row3);
        testMap.put(4,row4);
        testMap.put(5,row5);
        testMap.put(6,row6);
        return testMap;
    }

    public static Map<Integer, Map<Integer, Optional<Double>>> generateTestMap2() {
        Map<Integer, Map<Integer, Optional<Double>>> testMap = new HashMap<>();
        Map<Integer, Optional<Double>> row1 = new HashMap<>();
        Map<Integer, Optional<Double>> row2 = new HashMap<>();
        Map<Integer, Optional<Double>> row3 = new HashMap<>();
        Map<Integer, Optional<Double>> row4 = new HashMap<>();
        Map<Integer, Optional<Double>> row5 = new HashMap<>();
        row1.put(1,Optional.of((double)5));
        row2.put(1,Optional.of((double)4));
        row3.put(1,Optional.of((double)1));
        row4.put(1,Optional.empty());
        row5.put(1,Optional.of((double)1));
        row1.put(2,Optional.empty());
        row2.put(2,Optional.empty());
        row3.put(2,Optional.of((double)2));
        row4.put(2,Optional.of((double)2));
        row5.put(2,Optional.of((double)4));
        row1.put(3,Optional.of((double)5));
        row2.put(3,Optional.of((double)1));
        row3.put(3,Optional.of((double)4));
        row4.put(3,Optional.of((double)4));
        row5.put(3,Optional.of((double)1));
        row1.put(4,Optional.of((double)2));
        row2.put(4,Optional.of((double)2));
        row3.put(4,Optional.of((double)5));
        row4.put(4,Optional.of((double)1));
        row5.put(4,Optional.of((double)1));
        row1.put(5,Optional.of((double)5));
        row2.put(5,Optional.of((double)5));
        row3.put(5,Optional.empty());
        row4.put(5,Optional.of((double)1));
        row5.put(5,Optional.of((double)1));
        row1.put(6,Optional.empty());
        row2.put(6,Optional.of((double)4));
        row3.put(6,Optional.of((double)1));
        row4.put(6,Optional.of((double)2));
        row5.put(6,Optional.of((double)5));
        testMap.put(1,row1);
        testMap.put(2,row2);
        testMap.put(3,row3);
        testMap.put(4,row4);
        testMap.put(5,row5);
        return testMap;
    }


}
