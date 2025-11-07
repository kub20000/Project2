// 더미 데이터(화면 확인용)
const courseList = [
  { courseId: 1,  courseThumb: "img/exImg.png", courseTitle: "심폐소생술 기초", courseCategory: "자가 응급 처치", courseRegDate: "2025-01-05" },
  { courseId: 2,  courseThumb: "img/thumb02.jpg", courseTitle: "화재 발생 시 대처법", courseCategory: "구조자 응급 처치", courseRegDate: "2025-01-10" },
  { courseId: 3,  courseThumb: "img/thumb03.jpg", courseTitle: "응급 상황 전화 요령", courseCategory: "자가 응급 처치", courseRegDate: "2025-01-15" },
  { courseId: 4,  courseThumb: "img/thumb04.jpg", courseTitle: "자동심장충격기 사용법", courseCategory: "구조자 응급 처치", courseRegDate: "2025-01-20" },
  { courseId: 5,  courseThumb: "img/thumb05.jpg", courseTitle: "교통사고 현장 응급조치", courseCategory: "자가 응급 처치", courseRegDate: "2025-01-25" },
  { courseId: 6,  courseThumb: "img/thumb06.jpg", courseTitle: "화상 응급처치법", courseCategory: "구조자 응급 처치", courseRegDate: "2025-02-01" },
  { courseId: 7,  courseThumb: "img/thumb07.jpg", courseTitle: "기본 응급 키트 구성", courseCategory: "자가 응급 처치", courseRegDate: "2025-02-08" },
  { courseId: 8,  courseThumb: "img/thumb08.jpg", courseTitle: "심정지 대응 절차", courseCategory: "구조자 응급 처치", courseRegDate: "2025-02-14" },
  { courseId: 9,  courseThumb: "img/thumb09.jpg", courseTitle: "감전 시 응급처치", courseCategory: "자가 응급 처치", courseRegDate: "2025-02-20" },
  { courseId: 10, courseThumb: "img/thumb10.jpg", courseTitle: "응급 상황 대화 요령", courseCategory: "구조자 응급 처치", courseRegDate: "2025-02-25" },
  { courseId: 11, courseThumb: "img/thumb11.jpg", courseTitle: "학교 안전교육 영상", courseCategory: "자가 응급 처치", courseRegDate: "2025-03-01" },
  { courseId: 12, courseThumb: "img/thumb12.jpg", courseTitle: "자연재해 대피 요령", courseCategory: "구조자 응급 처치", courseRegDate: "2025-03-05" },
];

// 요소
const tbody = document.querySelector("#admin_courseList_table tbody");
const totalCount = document.getElementById("admin_tot_cnt");

// 테이블 렌더링
function renderTable(page) {
  tbody.innerHTML = "";

  const start = (page - 1) * rowsPerPage;
  const end = start + rowsPerPage;
  const pageData = courseList.slice(start, end);

  pageData.forEach((c, idx) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${start + idx + 1}</td>
      <td><img src="${c.courseThumb}" alt="${c.courseTitle}"></td>
      <td>
        <div class="course_title_box">
          <p class="course_category">[${c.courseCategory}]</p>
          <a href="adminCourseEdit.html?id=${c.courseId}" class="course_link">${c.courseTitle}</a>
        </div>
      </td>
      <td>관리자</td>
      <td>${c.courseRegDate}</td>
      <td><button class="admin_delete_btn" data-course-id="${c.courseId}">삭제</button></td>
    `;
    tbody.appendChild(tr);
  });

  totalCount.textContent = courseList.length.toString().padStart(2, "0");
  attachDeleteEvents();
}

// 삭제 버튼 이벤트
function attachDeleteEvents() {
  document.querySelectorAll(".admin_delete_btn").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      const courseId = parseInt(e.target.dataset.courseId);
      const confirmDelete = confirm("정말로 해당 강의를 삭제하시겠습니까?");
      if (confirmDelete) {
        const index = courseList.findIndex(c => c.courseId === courseId);
        if (index !== -1) {
          courseList.splice(index, 1);
          alert("강의가 삭제되었습니다.");
          renderTable(currentPage);
          renderPagination(courseList.length);
        }
      }
    });
  });
}

// 행 개수 변경
rowSelect.addEventListener("change", function () {
  rowsPerPage = parseInt(this.value);
  currentPage = 1;
  renderTable(currentPage);
  renderPagination(courseList.length);
});

// 강의 목록 페이징 처리
function changePage(page) {
  currentPage = page;
  renderTable(currentPage);
  renderPagination(courseList.length);
}

// 초기 실행
renderTable(currentPage);
renderPagination(courseList.length);
