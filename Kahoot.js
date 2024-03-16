const axios = require('axios');

const getQuizData = (uuid) => {
    return axios.get(`https://kahoot.it/rest/kahoots/${uuid}`)
        .then(response => {
            const data = response.data;
            if (!Array.isArray(data.questions)) {
                throw new Error('questions not iterable suka');
            }

            const questionList = data.questions
                .filter(slide => slide.type === "quiz")
                .map((slide, index) => {
                    const correctChoice = slide.choices.find(choice => choice.correct);
                    if (!correctChoice) return null;

                    const isTrueFalse = slide.choices.every(choice => choice.answer === "True" || choice.answer === "False");
                    const coloursList = (slide.choices.length === 2 && isTrueFalse) ? ["red", "blue"] : ["red", "blue", "yellow", "green"];

                    return {
                        number: index + 1,
                        type: "Quiz",
                        title: slide.question,
                        correctAnswer: correctChoice.answer,
                        buttonColor: coloursList[correctChoice.correct ? 0 : 1],
                        buttonIndex: correctChoice.correct ? 1 : 0
                    };
                })
                .filter(question => question !== null);

            return { entities: questionList };
        })
        .catch(error => {
            throw new Error('error fetching data nahui: ' + error.message);
        });
};

// example to run this ass
getQuizData("a1311b79-30f2-4815-86f4-7f75c23de52c").then(data => {
    data.entities.forEach(question => {
        console.log(question);
    });
}).catch(error => {
    console.error(error);
});
