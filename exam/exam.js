// 닫기버튼
document.addEventListener("DOMContentLoaded", () => {
    const closeBtn = document.getElementById("closeExam");

    closeBtn.addEventListener("click", (e) => {
        e.preventDefault();
        // 경고창 띄우기
        const confirmLeave = confirm("학습 결과가 저장되지 않습니다. 그래도 나가시겠습니까?");
        if (confirmLeave) {
            
            window.location.href = "/mypage/nonCompletion.html";
        }
        
    }); 
    });



// 해설 보기 토글 함수
function toggleExplanation(id) {
    const explanationContent = document.getElementById(id);
    const toggleButton = explanationContent.previousElementSibling; 

    if (explanationContent.style.display === "none") {
        explanationContent.style.display = "block";
        toggleButton.classList.add('open');
    } else {
        explanationContent.style.display = "none";
        toggleButton.classList.remove('open');
    }
}


// 최소 JS: 점수에 따라 합격/불합격 뱃지 표시
    const score = parseInt(document.querySelector('.final_score').textContent);
    const badge = document.getElementById('scoreBadge');
    if(score >= 60){
        badge.textContent = "합격";
        badge.classList.add("pass");
    } else {
        badge.textContent = "불합격";
        badge.classList.add("fail");
    }


//합격/불합격 -> 버튼 표시
    const completeBtn = document.getElementById('completeBtn');
    const retryBtn = document.getElementById('retryBtn');

    if(score >= 60){
        // 합격
        badge.textContent = "합격";
        badge.classList.add("pass");
        completeBtn.style.display = "inline-block";
        retryBtn.style.display = "inline-block";
    } else {
        // 불합격
        badge.textContent = "불합격";
        badge.classList.add("fail");
        completeBtn.style.display = "none";
        retryBtn.style.display = "inline-block";
    }