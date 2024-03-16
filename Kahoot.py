import requests

def getQuizData(uuid):
    url = f"https://kahoot.it/rest/kahoots/{uuid}"
    try:
        response = requests.get(url)
        response.raise_for_status()
        data = response.json()
        
        if not isinstance(data.get("questions"), list):
            raise ValueError("Questions not iterable suka")
        
        questions = []
        for index, slide in enumerate(data.get("questions", [])):
            if slide.get("type") == "quiz":
                istruefalse = all(choice.get("answer") in ["True", "False"] for choice in slide.get("choices", []))
                cols = ["red", "blue"] if len(slide.get("choices", [])) == 2 and istruefalse else ["red", "blue", "yellow", "green"]
                
                correct = next((choice for choice in slide.get("choices", []) if choice.get("correct")), None)
                index = slide.get("choices", []).index(correct) if correct else None
                
                question_obj = {
                    "number": index + 1,
                    "type": "Quiz",
                    "title": slide.get("question"),
                    "correctAnswer": correct.get("answer") if correct else None,
                    "buttonColor": cols[index] if index is not None else cols[0],
                    "buttonIndex": index if index is not None else 0
                }
                questions.append(question_obj)
        
        return {"entities": questions}
    except Exception as e:
        raise ValueError(f"error fetching data nahui: {str(e)}")

# patong example
try:
    data = getQuizData("put uuid here")
    for question in data.get("entities", []):
        output = "{" + "\n  " + "\n  ".join(f"{key}: {value!r}" for key, value in question.items()) + "\n}"
        print(output)
except ValueError as ve:
    print(ve)
