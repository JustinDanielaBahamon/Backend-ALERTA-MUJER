import { Router } from 'express';
import { getMe, updateMe } from '../controllers/users';
import { authMiddleware } from '../../../shared/src/jwt';

const router = Router();

router.get('/me', authMiddleware, getMe);
router.put('/me', authMiddleware, updateMe);

export default router;
