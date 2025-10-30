// Data Storage
let albums = JSON.parse(localStorage.getItem('polaroidAlbums')) || [];
let currentAlbum = null;
let currentImageData = null;
let currentPhotoData = null;

// DOM Elements
const albumsGrid = document.getElementById('albumsGrid');
const emptyState = document.getElementById('emptyState');
const albumModal = document.getElementById('albumModal');
const albumDetailModal = document.getElementById('albumDetailModal');
const photoModal = document.getElementById('photoModal');
const addAlbumBtn = document.getElementById('addAlbumBtn');
const saveBtn = document.getElementById('saveBtn');
const cancelBtn = document.getElementById('cancelBtn');
const imageInput = document.getElementById('imageInput');
const previewImage = document.getElementById('previewImage');
const closeDetailBtn = document.getElementById('closeDetailBtn');
const addPhotoBtn = document.getElementById('addPhotoBtn');
const photoInput = document.getElementById('photoInput');
const photoPreview = document.getElementById('photoPreview');
const savePhotoBtn = document.getElementById('savePhotoBtn');
const cancelPhotoBtn = document.getElementById('cancelPhotoBtn');

// Initialize
renderAlbums();

// Event Listeners
addAlbumBtn.addEventListener('click', openAlbumModal);
saveBtn.addEventListener('click', saveAlbum);
cancelBtn.addEventListener('click', closeAlbumModal);
closeDetailBtn.addEventListener('click', closeDetailModal);
addPhotoBtn.addEventListener('click', openPhotoModal);
savePhotoBtn.addEventListener('click', savePhoto);
cancelPhotoBtn.addEventListener('click', closePhotoModal);

imageInput.addEventListener('change', (e) => {
    const file = e.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            currentImageData = e.target.result;
            previewImage.innerHTML = `<img src="${currentImageData}" alt="Preview">`;
        };
        reader.readAsDataURL(file);
    }
});

photoInput.addEventListener('change', (e) => {
    const file = e.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            currentPhotoData = e.target.result;
            photoPreview.innerHTML = `<img src="${currentPhotoData}" alt="Preview">`;
        };
        reader.readAsDataURL(file);
    }
});

// Modal Click Outside
albumModal.addEventListener('click', (e) => {
    if (e.target === albumModal) closeAlbumModal();
});

albumDetailModal.addEventListener('click', (e) => {
    if (e.target === albumDetailModal) closeDetailModal();
});

photoModal.addEventListener('click', (e) => {
    if (e.target === photoModal) closePhotoModal();
});

// Functions
function renderAlbums() {
    if (albums.length === 0) {
        emptyState.style.display = 'block';
        albumsGrid.style.display = 'none';
    } else {
        emptyState.style.display = 'none';
        albumsGrid.style.display = 'grid';
        
        albumsGrid.innerHTML = albums.map((album, index) => `
            <div class="polaroid-card" onclick="openAlbumDetail(${index})">
                <button class="delete-btn" onclick="event.stopPropagation(); deleteAlbum(${index})" title="삭제">✕</button>
                <div class="polaroid-image">
                    ${album.image ? `<img src="${album.image}" alt="${album.title}">` : '📷'}
                </div>
                <div class="polaroid-text">
                    <div class="polaroid-title">${album.title || '제목 없음'}</div>
                    <div class="polaroid-date">${album.date || ''}</div>
                </div>
            </div>
        `).join('');
    }
}

function openAlbumModal() {
    albumModal.classList.add('active');
    document.getElementById('albumTitle').value = '';
    document.getElementById('albumDate').value = '';
    document.getElementById('albumMemo').value = '';
    currentImageData = null;
    previewImage.innerHTML = '<span class="preview-placeholder">사진을 선택하세요</span>';
}

function closeAlbumModal() {
    albumModal.classList.remove('active');
}

function saveAlbum() {
    const title = document.getElementById('albumTitle').value.trim();
    const date = document.getElementById('albumDate').value.trim();
    const memo = document.getElementById('albumMemo').value.trim();
    
    if (!title) {
        alert('제목을 입력해주세요');
        return;
    }
    
    const newAlbum = {
        id: Date.now(),
        title,
        date,
        memo,
        image: currentImageData,
        photos: []
    };
    
    albums.push(newAlbum);
    localStorage.setItem('polaroidAlbums', JSON.stringify(albums));
    
    renderAlbums();
    closeAlbumModal();
    
    // Show success message
    showToast('앨범이 생성되었습니다 ✨');
}

function deleteAlbum(index) {
    if (confirm(`'${albums[index].title}' 앨범을 삭제하시겠습니까?\n(앨범 내 모든 사진이 삭제됩니다)`)) {
        albums.splice(index, 1);
        localStorage.setItem('polaroidAlbums', JSON.stringify(albums));
        renderAlbums();
        showToast('앨범이 삭제되었습니다');
    }
}

function openAlbumDetail(index) {
    currentAlbum = albums[index];
    
    document.getElementById('detailTitle').textContent = currentAlbum.title;
    document.getElementById('detailDate').textContent = currentAlbum.date;
    
    const memoElement = document.getElementById('detailMemo');
    if (currentAlbum.memo) {
        memoElement.textContent = currentAlbum.memo;
        memoElement.style.display = 'block';
    } else {
        memoElement.style.display = 'none';
    }
    
    renderPhotos();
    albumDetailModal.classList.add('active');
}

function closeDetailModal() {
    albumDetailModal.classList.remove('active');
    currentAlbum = null;
}

function renderPhotos() {
    const photosGrid = document.getElementById('photosGrid');
    const photosEmpty = document.getElementById('photosEmpty');
    
    if (!currentAlbum.photos || currentAlbum.photos.length === 0) {
        photosEmpty.style.display = 'block';
        photosGrid.style.display = 'none';
    } else {
        photosEmpty.style.display = 'none';
        photosGrid.style.display = 'grid';
        
        photosGrid.innerHTML = currentAlbum.photos.map((photo, index) => `
            <div class="photo-card">
                <button class="delete-btn" onclick="deletePhoto(${index})" title="삭제">✕</button>
                <img src="${photo.image}" alt="Photo">
                <div class="photo-info">
                    ${photo.memo ? `<div class="photo-memo">${photo.memo}</div>` : ''}
                    ${photo.date ? `<div class="photo-date">${photo.date}</div>` : ''}
                </div>
            </div>
        `).join('');
    }
}

function openPhotoModal() {
    if (currentAlbum.photos.length >= 20) {
        alert('사진은 최대 20장까지 추가할 수 있어요');
        return;
    }
    
    photoModal.classList.add('active');
    document.getElementById('photoDate').value = '';
    document.getElementById('photoMemo').value = '';
    currentPhotoData = null;
    photoPreview.innerHTML = '<span class="preview-placeholder">사진을 선택하세요</span>';
}

function closePhotoModal() {
    photoModal.classList.remove('active');
}

function savePhoto() {
    if (!currentPhotoData) {
        alert('사진을 선택해주세요');
        return;
    }
    
    const date = document.getElementById('photoDate').value.trim();
    const memo = document.getElementById('photoMemo').value.trim();
    
    const newPhoto = {
        id: Date.now(),
        image: currentPhotoData,
        date,
        memo
    };
    
    currentAlbum.photos.push(newPhoto);
    
    // Update albums array
    const albumIndex = albums.findIndex(a => a.id === currentAlbum.id);
    if (albumIndex !== -1) {
        albums[albumIndex] = currentAlbum;
        localStorage.setItem('polaroidAlbums', JSON.stringify(albums));
    }
    
    renderPhotos();
    closePhotoModal();
    showToast('사진이 추가되었습니다 📸');
}

function deletePhoto(index) {
    if (confirm('이 사진을 삭제하시겠습니까?')) {
        currentAlbum.photos.splice(index, 1);
        
        // Update albums array
        const albumIndex = albums.findIndex(a => a.id === currentAlbum.id);
        if (albumIndex !== -1) {
            albums[albumIndex] = currentAlbum;
            localStorage.setItem('polaroidAlbums', JSON.stringify(albums));
        }
        
        renderPhotos();
        showToast('사진이 삭제되었습니다');
    }
}

function showToast(message) {
    const toast = document.createElement('div');
    toast.style.cssText = `
        position: fixed;
        bottom: 100px;
        left: 50%;
        transform: translateX(-50%);
        background: rgba(0,0,0,0.8);
        color: white;
        padding: 12px 24px;
        border-radius: 24px;
        font-size: 14px;
        z-index: 9999;
        animation: fadeInOut 2s ease-in-out;
    `;
    toast.textContent = message;
    document.body.appendChild(toast);
    
    setTimeout(() => {
        document.body.removeChild(toast);
    }, 2000);
}

// Add fade animation
const style = document.createElement('style');
style.textContent = `
    @keyframes fadeInOut {
        0%, 100% { opacity: 0; transform: translateX(-50%) translateY(20px); }
        10%, 90% { opacity: 1; transform: translateX(-50%) translateY(0); }
    }
`;
document.head.appendChild(style);
