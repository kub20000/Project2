// 썸네일 미리보기
const thumbInput = document.getElementById("editCourseThumb");
const thumbPreview = document.getElementById("editThumbPreview");

if (thumbInput) {
  thumbInput.addEventListener("change", function (event) {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();

      reader.onload = function (e) {
        thumbPreview.src = e.target.result;
        thumbPreview.style.display = "block";
      };

      reader.readAsDataURL(file);
    } else {
      // 파일이 제거되었을 때 미리보기 숨김
      thumbPreview.src = "";
      thumbPreview.style.display = "none";
    }
  });
}